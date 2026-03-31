# Doto — API Spec: Child Profile Claim
**Version:** 1.0
**Feature:** Linking a child's self-registered account to an existing parent-created profile
**Scope:** Backend (Scala Play)
**Depends on:** ONBOARDING_API_SPEC.md — specifically the families/preview endpoint and register flow

---

## 1. Overview

When a parent adds a child profile via `FamilyManageView`, it creates a placeholder
row in the `profiles` table with `is_auth_account = false`. The child has no credentials
and cannot log in.

When that child later gets their own device and wants to use the app, they go through
the join flow using the family code. At this point two things could happen:

1. They create a brand new profile — duplicate Liam appears, existing task history lost
2. They claim the existing placeholder — credentials added to the existing row, all
   history preserved

This spec defines the API changes to support Option 2: the **claim flow**.

---

## 2. Concepts

**Unclaimed child profile** — a `profiles` row where:
- `role = 'child'`
- `is_auth_account = false`
- `username IS NULL`
- `password_hash IS NULL`
- `family_id` is set

**Claimed child profile** — the same row after the child registers:
- `is_auth_account = true`
- `username` and `password_hash` set
- All existing `points`, `streak`, task assignments, and reward goals preserved

---

## 3. Database Changes

No new tables required. The existing `profiles` table already has all the columns
needed. The claim operation is purely an `UPDATE` on an existing row.

Confirm these nullable columns exist on `profiles` (should already be present from
ONBOARDING_API_SPEC.md):

```sql
-- Verify these are nullable (not NOT NULL) in evolution 1.sql:
username        VARCHAR(50)   UNIQUE,       -- NULL for unclaimed profiles
password_hash   VARCHAR(255),              -- NULL for unclaimed profiles
```

No migration needed if the above is already correct.

---

## 4. Modified Endpoint

### 4.1 GET /api/families/preview/:code

**Extended** to include unclaimed child profiles. No auth required.

Previously returned:
```json
{
  "familyName": "The Smith Family",
  "memberCount": 3,
  "inviteCode": "DOTO4X"
}
```

**Now returns:**
```json
{
  "familyName": "The Smith Family",
  "memberCount": 3,
  "inviteCode": "DOTO4X",
  "unclaimedChildren": [
    {
      "id": "uuid-liam",
      "displayName": "Liam",
      "color": "#BA7517"
    },
    {
      "id": "uuid-emma",
      "displayName": "Emma",
      "color": "#993556"
    }
  ]
}
```

**`unclaimedChildren` field:**
- Array of profiles where `role = 'child'` AND `is_auth_account = false`
  AND `family_id` matches the code's family
- Empty array `[]` if no unclaimed children exist — never null
- Returns only `id`, `displayName`, `color` — no sensitive data since this
  endpoint requires no auth

**Backend query addition:**
```scala
// In FamilyRepository.previewByCode():
val unclaimedChildren = profileRepo.findUnclaimedChildren(family.id)
// SELECT id, display_name, color FROM profiles
// WHERE family_id = :familyId
//   AND role = 'child'
//   AND is_auth_account = false
```

---

## 5. New Endpoints

### 5.1 POST /api/auth/claim-profile

Converts an existing unclaimed child placeholder into a full auth account.
No existing JWT required — the child does not have an account yet.

**Request body:**
```json
{
  "profileId": "uuid-liam",
  "inviteCode": "DOTO4X",
  "username": "liam_smith",
  "password": "mypassword99"
}
```

| Field | Type | Required | Rules |
|---|---|---|---|
| profileId | UUID string | yes | Must be an unclaimed child profile in the family matching inviteCode |
| inviteCode | string | yes | 6 chars, case-insensitive — used to verify the profile belongs to the right family |
| username | string | yes | 3–50 chars, `^[a-z0-9_]+$`, globally unique |
| password | string | yes | Minimum 8 characters |

**Backend logic:**
```scala
// In AuthController.claimProfile():

// 1. Normalise invite code to uppercase
val code = req.inviteCode.trim.toUpperCase

// 2. Look up family by invite code
familyRepo.findByInviteCode(code) flatMap {
  case None => Future.successful(
    NotFound(Json.toJson(ApiError("not_found", "Invite code not found")))
  )
  case Some(family) =>

    // 3. Look up the profile
    profileRepo.findById(req.profileId) flatMap {
      case None => Future.successful(
        NotFound(Json.toJson(ApiError("not_found", "Profile not found")))
      )
      case Some(profile) =>

        // 4. Verify the profile belongs to the same family as the code
        if (profile.familyId != Some(family.id)) {
          Future.successful(
            Forbidden(Json.toJson(ApiError("forbidden", "Profile does not belong to this family")))
          )
        }
        // 5. Verify the profile is actually unclaimed
        else if (profile.isAuthAccount) {
          Future.successful(
            Conflict(Json.toJson(ApiError("conflict", "This profile already has an account")))
          )
        }
        else {
          // 6. Check username uniqueness
          profileRepo.findByUsername(req.username.toLowerCase) flatMap {
            case Some(_) => Future.successful(
              Conflict(Json.toJson(ApiError("conflict", "That username is already taken")))
            )
            case None =>
              // 7. Hash the password
              val hash = BCrypt.hashpw(req.password, BCrypt.gensalt(12))

              // 8. UPDATE the existing profile row — preserve all existing data
              profileRepo.claimProfile(
                profileId    = profile.id,
                username     = req.username.toLowerCase,
                passwordHash = hash
              ) map { updatedProfile =>

                // 9. Generate JWT for the now-claimed profile
                val token = jwtService.generateToken(updatedProfile.id, updatedProfile.familyId)

                Created(Json.toJson(AuthResponse(token, updatedProfile)))
              }
          }
        }
    }
}
```

**ProfileRepository.claimProfile() SQL:**
```scala
// UPDATE profiles SET
//   username         = :username,
//   password_hash    = :passwordHash,
//   is_auth_account  = true,
//   updated_at       = NOW()
// WHERE id = :profileId
//   AND is_auth_account = false   ← extra guard at DB level
```

**Response `201 Created`:**
```json
{
  "token": "eyJhbGci...",
  "profile": {
    "id": "uuid-liam",
    "username": "liam_smith",
    "displayName": "Liam",
    "role": "child",
    "color": "#BA7517",
    "points": 145,
    "streak": 5,
    "familyId": "family-uuid",
    "isAuthAccount": true,
    "createdAt": "2026-01-15T10:00:00Z"
  }
}
```

Note `points: 145` and `streak: 5` — the child's existing history is fully preserved
because we updated the existing row rather than creating a new one.

**Errors:**
| HTTP | code | When |
|---|---|---|
| 404 | `not_found` | Invite code not found |
| 404 | `not_found` | Profile ID not found |
| 403 | `forbidden` | Profile doesn't belong to the family matching the code |
| 409 | `conflict` | Profile already has an account (`is_auth_account = true`) |
| 409 | `conflict` | Username already taken by another account |
| 400 | `validation_error` | Username format invalid, password too short |

---

### 5.2 GET /api/members/:id/claim-status

Allows a parent to check whether an unclaimed child profile has been claimed yet.
Useful for the Settings screen to show "No account yet" vs a username.

**Auth required.** Caller must be a parent in the same family as the profile.

**Response `200 OK`:**
```json
{
  "profileId": "uuid-liam",
  "displayName": "Liam",
  "isClaimed": false,
  "username": null
}
```

Or when claimed:
```json
{
  "profileId": "uuid-liam",
  "displayName": "Liam",
  "isClaimed": true,
  "username": "liam_smith"
}
```

**Errors:**
- `403 forbidden` — profile not in caller's family
- `404 not_found` — profile not found

---

## 6. Updated Routes File

Add to `conf/routes`:

```
# Child profile claim
POST    /api/auth/claim-profile          controllers.AuthController.claimProfile
GET     /api/members/:id/claim-status    controllers.MemberController.claimStatus(id: String)
```

The existing `GET /api/families/preview/:code` route is unchanged — it just returns
more data now.

---

## 7. Member List Response Update

The `GET /api/members` endpoint response for each member should now include
`isAuthAccount` and `username` so the parent-facing member list can distinguish
between claimed and unclaimed children:

```json
[
  {
    "id": "uuid-sarah",
    "displayName": "Sarah",
    "role": "parent",
    "color": "#185FA5",
    "points": 120,
    "streak": 3,
    "isAuthAccount": true,
    "username": "sarah_smith"
  },
  {
    "id": "uuid-liam",
    "displayName": "Liam",
    "role": "child",
    "color": "#BA7517",
    "points": 145,
    "streak": 5,
    "isAuthAccount": true,
    "username": "liam_smith"
  },
  {
    "id": "uuid-emma",
    "displayName": "Emma",
    "role": "child",
    "color": "#993556",
    "points": 30,
    "streak": 0,
    "isAuthAccount": false,
    "username": null
  }
]
```

Emma has `isAuthAccount: false` and `username: null` — parent can see she hasn't
claimed her account yet.

---

## 8. Security Notes

1. **Double verification.** The claim endpoint requires both the profile ID and the
   invite code. The profile ID alone is not enough — the invite code confirms the
   child is actually joining the right family. This prevents someone from claiming
   an arbitrary profile by guessing a UUID.

2. **DB-level guard.** The `UPDATE` query includes `AND is_auth_account = false` as
   a WHERE condition. Even if the application logic fails, the DB will not update a
   profile that is already claimed.

3. **Username uniqueness** is enforced at the DB level via the `UNIQUE` constraint on
   `profiles.username`. The application checks first and returns a clean 409 before
   hitting the constraint.

4. **No data is lost.** The claim operation is an UPDATE on the existing row. Points,
   streak, task assignments, reward goals, and all other data tied to the profile ID
   are untouched.

5. **The invite code is not a secret.** An attacker who knows both the invite code and
   a valid unclaimed profile ID could claim that profile. In practice this is low risk
   — the profile ID is a UUID (not guessable), and a successful claim only gives
   access to a child-role account with restricted permissions. Monitor for abuse in V2.
