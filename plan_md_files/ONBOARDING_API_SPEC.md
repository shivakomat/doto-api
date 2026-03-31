# Doto — API Spec: Onboarding
**Version:** 2.0
**Scope:** Auth, registration, family join via 6-char code, child accounts
**Depends on:** Core API_SPEC.md — this document extends and overrides specific endpoints

---

## 1. Core Principles

- **No email anywhere.** No email field, no email storage, no SMTP, no password reset
  via email. Username is the sole login identifier for all users.
- **The family code is universal.** Both parents and children use the same 6-char code
  to join an existing family. Role is self-declared during registration.
- **Code-first flow.** Users who have a family code enter it before registering so they
  can see which family they are joining. Registration and family joining happen in a
  single transaction.
- **Children choose their own credentials.** No parent-generated temporary passwords.
  Children pick their own username and password using the same rules as parents.

---

## 2. Database Changes

### 2.1 Profiles Table — Final Schema

The `profiles` table uses `username` as the sole login identifier. No email column.

```sql
-- Evolution 1.sql — ensure these columns are present, remove email if it exists

CREATE TABLE profiles (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    family_id       UUID            REFERENCES families(id) ON DELETE SET NULL,
    username        VARCHAR(50)     NOT NULL UNIQUE,
    password_hash   VARCHAR(255)    NOT NULL,
    display_name    VARCHAR(100)    NOT NULL,
    role            VARCHAR(10)     NOT NULL CHECK (role IN ('parent', 'child')),
    color           VARCHAR(7)      NOT NULL DEFAULT '#6C63FF',
    points          INTEGER         NOT NULL DEFAULT 0,
    streak          INTEGER         NOT NULL DEFAULT 0,
    last_streak_date DATE,
    is_auth_account BOOLEAN         NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_profiles_family_id ON profiles(family_id);
CREATE INDEX idx_profiles_username  ON profiles(username);
```

**No email column.** If the existing evolution has an email column, remove it:
```sql
ALTER TABLE profiles DROP COLUMN IF EXISTS email;
```

### 2.2 Families Table — Confirm Invite Code

The `invite_code` column must exist on `families`:
```sql
-- Should already be in evolution 1.sql — confirm only:
-- invite_code  CHAR(6) NOT NULL UNIQUE
-- CREATE INDEX idx_families_invite_code ON families(invite_code);
```

---

## 3. Invite Code Generation

```
Character set: ABCDEFGHJKLMNPQRSTUVWXYZ23456789
  Excludes: O (looks like 0), 0 (looks like O), I (looks like 1), 1 (looks like I)
Length: 6 characters
Expiry: never — permanent for MVP
Uniqueness: enforced by DB UNIQUE constraint, retry up to 5 times on collision
```

```scala
// app/services/InviteService.scala
@Singleton
class InviteService @Inject()() {
  private val charset = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789"

  def generateCode(): String =
    (1 to 6).map(_ => charset(scala.util.Random.nextInt(charset.length))).mkString
}
```

---

## 4. Colour Auto-Assignment

When any user joins a family (whether parent or child), they get the next available
colour from the palette in order:

```scala
val palette = List("#185FA5","#1D9E75","#BA7517","#993556","#534AB7","#E24B4A")

def assignNextColor(existingMembers: Seq[Profile]): String = {
  val used = existingMembers.map(_.color).toSet
  palette.find(c => !used.contains(c)).getOrElse(palette(existingMembers.size % palette.size))
}
```

The family creator always gets `#185FA5` (blue). Each subsequent member gets the next
unused colour in the sequence regardless of their role.

---

## 5. Endpoints

### 5.1 GET /api/families/preview/:code

**No auth required.** Called before registration to show the user which family they are
about to join. This makes the join flow feel intentional — the user sees the family name
before creating their account.

**Path param:** `code` — the 6-char invite code (case-insensitive, normalised to uppercase)

**Response `200 OK`:**
```json
{
  "familyName": "The Smith Family",
  "memberCount": 2,
  "inviteCode": "DOTO4X"
}
```

`memberCount` gives the joining user a sense of how many people are already in the family.

**Errors:**
- `404 not_found` — code does not match any family

**Note:** This endpoint deliberately returns minimal data. It does not return member
names, IDs, or any PII. It exists only to confirm the code is valid and show the
family name.

---

### 5.2 POST /api/auth/register

Creates a new user account. Optionally joins a family in the same transaction.

**Request body:**
```json
{
  "username": "sarah_smith",
  "password": "mypassword123",
  "displayName": "Sarah",
  "role": "parent",
  "inviteCode": "DOTO4X"
}
```

| Field | Type | Required | Rules |
|---|---|---|---|
| username | string | yes | 3–50 chars, alphanumeric + underscore only (`^[a-z0-9_]+$`), lowercase enforced, globally unique |
| password | string | yes | Minimum 8 characters |
| displayName | string | yes | 1–100 chars |
| role | string | yes | `"parent"` or `"child"` |
| inviteCode | string | no | If provided: 6 chars, validated against families table, user is joined to that family as part of registration |

**Role rules:**
- `role: "child"` is only valid when `inviteCode` is also provided. A child cannot
  exist without a family. Return `400 validation_error` if `role = "child"` and no
  `inviteCode`.
- `role: "parent"` with no `inviteCode` → user registers without a family, goes through
  Family Setup flow.
- `role: "parent"` with `inviteCode` → user registers and joins the family as a parent.

**Backend logic:**
```scala
// In AuthController.register():

// 1. Validate username format and uniqueness
// 2. Hash password with bcrypt cost 12
// 3. Validate role + inviteCode combination
// 4. If inviteCode provided:
//    a. Look up family by invite_code (case-insensitive)
//    b. If not found: return 404
//    c. Assign next colour from palette
//    d. INSERT profile with family_id = family.id
// 5. If no inviteCode:
//    a. INSERT profile with family_id = null
// 6. Generate JWT containing userId + familyId (nullable)
// 7. Return token + profile
```

**Response `201 Created`:**
```json
{
  "token": "eyJhbGci...",
  "profile": {
    "id": "uuid",
    "username": "sarah_smith",
    "displayName": "Sarah",
    "role": "parent",
    "color": "#185FA5",
    "points": 0,
    "streak": 0,
    "familyId": "uuid",
    "isAuthAccount": true,
    "createdAt": "2026-03-27T10:00:00Z"
  }
}
```

`familyId` is `null` when no `inviteCode` was provided. The iOS app uses this to decide
whether to show Family Setup or go straight to Dashboard.

**Errors:**
- `409 conflict` — username already taken
- `400 validation_error` — username format invalid, password too short, role=child without inviteCode
- `404 not_found` — inviteCode provided but not found (return this, not a generic 400, so iOS can show the right message)

---

### 5.3 POST /api/auth/login

Authenticate with username and password. Works for all roles.

**Request body:**
```json
{
  "username": "sarah_smith",
  "password": "mypassword123"
}
```

| Field | Type | Required | Rules |
|---|---|---|---|
| username | string | yes | Case-insensitive lookup (normalise to lowercase before querying) |
| password | string | yes | |

**Response `200 OK`:**
```json
{
  "token": "eyJhbGci...",
  "profile": {
    "id": "uuid",
    "username": "sarah_smith",
    "displayName": "Sarah",
    "role": "parent",
    "color": "#185FA5",
    "points": 0,
    "streak": 0,
    "familyId": "uuid",
    "isAuthAccount": true,
    "createdAt": "2026-03-27T10:00:00Z"
  }
}
```

**Errors:**
- `401 unauthorized` — wrong username or password. Never distinguish which is wrong.

---

### 5.4 GET /api/auth/me

Returns the currently authenticated user's profile. Called on app launch to restore session.

**Response `200 OK`:** Single profile object (same shape as login response profile).

---

### 5.5 POST /api/families (Create Family)

No change from core API_SPEC.md. Creates a family, assigns the creator as the first parent,
generates and stores the invite code, auto-creates the "Groceries" shopping list.

**Request body:**
```json
{ "name": "The Smith Family" }
```

**Response `201 Created`:**
```json
{
  "id": "family-uuid",
  "name": "The Smith Family",
  "inviteCode": "DOTO4X",
  "members": [
    {
      "id": "uuid",
      "username": "sarah_smith",
      "displayName": "Sarah",
      "role": "parent",
      "color": "#185FA5",
      "points": 0,
      "streak": 0,
      "isAuthAccount": true
    }
  ],
  "createdAt": "2026-03-27T10:00:00Z"
}
```

---

### 5.6 POST /api/families/join

For authenticated users who already have an account and want to join a family after
the fact. Uses the same 6-char invite code. Role is passed in the request body.

**Request body:**
```json
{
  "inviteCode": "DOTO4X",
  "role": "parent"
}
```

| Field | Type | Required | Rules |
|---|---|---|---|
| inviteCode | string | yes | 6 chars, case-insensitive |
| role | string | yes | `"parent"` or `"child"` |

**Backend logic:**
1. Normalise code to uppercase
2. Look up family by invite_code
3. Check caller does not already have a familyId
4. Assign next colour
5. Update caller's profile: `family_id = family.id`, `role = role`, `color = nextColor`
6. Return updated family object

**Response `200 OK`:** Same shape as `POST /api/families` response.

**Errors:**
- `404 not_found` — invite code not found
- `409 conflict` — caller already belongs to a family

---

### 5.7 GET /api/families/mine/invite-code

Returns the invite code for the calling user's family. Used in Settings so parents
can re-share the code at any time.

**Response `200 OK`:**
```json
{
  "inviteCode": "DOTO4X",
  "familyName": "The Smith Family"
}
```

**Errors:**
- `404 not_found` — user has no family

---

### 5.8 PATCH /api/auth/change-password

Allows any authenticated user to change their own password.

**Request body:**
```json
{
  "currentPassword": "oldpassword",
  "newPassword": "newpassword99"
}
```

| Field | Type | Required | Rules |
|---|---|---|---|
| currentPassword | string | yes | Must match stored bcrypt hash |
| newPassword | string | yes | Minimum 8 characters |

**Response `200 OK`:**
```json
{ "updated": true }
```

**Errors:**
- `401 unauthorized` — currentPassword does not match
- `400 validation_error` — newPassword too short

---

## 6. Profile Response Shape

All endpoints that return a profile object use this consistent shape:

```json
{
  "id": "uuid",
  "username": "liam_smith",
  "displayName": "Liam",
  "role": "child",
  "color": "#BA7517",
  "points": 85,
  "streak": 5,
  "familyId": "family-uuid",
  "isAuthAccount": true,
  "createdAt": "2026-03-27T10:00:00Z"
}
```

**No email field anywhere.** Username is the sole identifier.

---

## 7. Updated Routes File

```
# Auth
POST    /api/auth/register               controllers.AuthController.register
POST    /api/auth/login                  controllers.AuthController.login
GET     /api/auth/me                     controllers.AuthController.me
PATCH   /api/auth/change-password        controllers.AuthController.changePassword

# Families
GET     /api/families/preview/:code      controllers.FamilyController.preview(code: String)
POST    /api/families                    controllers.FamilyController.create
POST    /api/families/join               controllers.FamilyController.join
GET     /api/families/mine               controllers.FamilyController.mine
GET     /api/families/mine/invite-code   controllers.FamilyController.inviteCode
```

---

## 8. Registration Flow Summary

```
┌─────────────────────────────────────────────────────┐
│  User has a family code?                            │
└──────────┬──────────────────────────┬───────────────┘
           │ YES                      │ NO
           ▼                          ▼
GET /api/families/preview/:code    User registers:
Returns family name                POST /api/auth/register
           │                       { username, password,
           ▼                         displayName,
User sees: "Joining               role: "parent",
The Smith Family"                  inviteCode: null }
           │                          │
           ▼                          ▼
User registers:                   familyId = null in response
POST /api/auth/register            → iOS shows FamilySetupView
{ username, password,                 │
  displayName,                        ▼
  role: "parent"|"child",         POST /api/families
  inviteCode: "DOTO4X" }          Creates family, gets invite code
           │                          │
           ▼                          ▼
familyId set in response          → iOS shows NotificationsView
→ iOS goes straight to            → Dashboard (empty state)
  Dashboard (skips FamilySetup)
```

---

## 9. Security Notes

1. **Username uniqueness** is enforced at the DB level (`UNIQUE` constraint). The API
   returns `409 conflict` on collision so the iOS client can prompt the user to choose
   a different username.

2. **Username normalisation** — store and query usernames in lowercase. The registration
   endpoint lowercases before inserting. The login endpoint lowercases before querying.
   This prevents `Sarah_Smith` and `sarah_smith` being treated as different accounts.

3. **Invite code is not a security boundary.** It is a convenience mechanism. Family
   data isolation is enforced by `familyId` in the JWT on every request — a user can
   only access data for the family embedded in their token, regardless of the invite code.

4. **Password hashing** — bcrypt with cost factor 12 on all passwords. No plaintext
   passwords stored or logged anywhere.

5. **Role validation** — the backend validates that `role = "child"` is only accepted
   when an `inviteCode` is also provided. A child profile without a family is invalid
   and must never be created.
