# Doto — Data Model
**Version:** 1.0  
**Database:** PostgreSQL 15  
**Migration tool:** Play Evolutions (files in `conf/evolutions/default/`)

---

## 1. Overview

The schema has 8 tables. Apply migrations in order — each file is additive.

```
families
    └── profiles          (family members — parents and child profiles)
    └── events            (calendar events)
    └── tasks             (household tasks)
    └── shopping_lists    (named lists per family)
    │       └── shopping_items
    └── rewards           (kid reward goals)
```

**Key design decisions:**
- All primary keys are **UUIDs** (gen_random_uuid()) — safe to generate on either client or server
- All timestamps are **TIMESTAMPTZ** (UTC stored, display conversion is client's responsibility)
- `family_id` is on every table — all repository queries filter by it for data isolation
- Child members are rows in `profiles` with `role = 'child'` and `is_auth_account = false` — they do not log in
- `events.assigned_to` is a UUID array — one event can be assigned to multiple family members

---

## 2. Evolution Files

### `conf/evolutions/default/1.sql` — Families & Profiles

```sql
-- !Ups

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE families (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name         VARCHAR(100)  NOT NULL,
    invite_code  CHAR(6)       NOT NULL UNIQUE,
    created_at   TIMESTAMPTZ   NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ   NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_families_invite_code ON families(invite_code);

CREATE TABLE profiles (
    id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    family_id       UUID            REFERENCES families(id) ON DELETE SET NULL,
    username        VARCHAR(50)     UNIQUE,          -- NULL for child profiles
    password_hash   VARCHAR(255),                    -- NULL for child profiles
    display_name    VARCHAR(100)    NOT NULL,
    role            VARCHAR(10)     NOT NULL CHECK (role IN ('parent', 'child')),
    color           VARCHAR(7)      NOT NULL DEFAULT '#6C63FF',  -- hex colour for avatar
    points          INTEGER         NOT NULL DEFAULT 0,
    is_auth_account BOOLEAN         NOT NULL DEFAULT TRUE,       -- FALSE for child profiles
    created_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_profiles_family_id   ON profiles(family_id);
CREATE INDEX idx_profiles_username    ON profiles(username);

-- !Downs

DROP TABLE IF EXISTS profiles;
DROP TABLE IF EXISTS families;
```

---

### `conf/evolutions/default/2.sql` — Events

```sql
-- !Ups

CREATE TABLE events (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    family_id   UUID            NOT NULL REFERENCES families(id) ON DELETE CASCADE,
    title       VARCHAR(200)    NOT NULL,
    description TEXT,
    start_at    TIMESTAMPTZ     NOT NULL,
    end_at      TIMESTAMPTZ     NOT NULL,
    location    VARCHAR(300),
    color       VARCHAR(7),                         -- optional override, else use assignee colour
    assigned_to UUID[]          NOT NULL DEFAULT '{}',  -- array of profile IDs
    created_by  UUID            NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),

    CONSTRAINT events_end_after_start CHECK (end_at > start_at)
);

CREATE INDEX idx_events_family_id ON events(family_id);
CREATE INDEX idx_events_start_at  ON events(family_id, start_at);

-- !Downs

DROP TABLE IF EXISTS events;
```

---

### `conf/evolutions/default/3.sql` — Tasks

```sql
-- !Ups

CREATE TABLE tasks (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    family_id   UUID            NOT NULL REFERENCES families(id) ON DELETE CASCADE,
    title       VARCHAR(200)    NOT NULL,
    description TEXT,
    assigned_to UUID            REFERENCES profiles(id) ON DELETE SET NULL,
    status      VARCHAR(20)     NOT NULL DEFAULT 'todo'
                                CHECK (status IN ('todo', 'in_progress', 'done', 'cancelled')),
    priority    VARCHAR(10)     NOT NULL DEFAULT 'medium'
                                CHECK (priority IN ('low', 'medium', 'high')),
    points      INTEGER         NOT NULL DEFAULT 0 CHECK (points >= 0),
    due_at      TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_by  UUID            NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tasks_family_id   ON tasks(family_id);
CREATE INDEX idx_tasks_assigned_to ON tasks(assigned_to);
CREATE INDEX idx_tasks_status      ON tasks(family_id, status);

-- !Downs

DROP TABLE IF EXISTS tasks;
```

---

### `conf/evolutions/default/4.sql` — Shopping

```sql
-- !Ups

CREATE TABLE shopping_lists (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    family_id   UUID            NOT NULL REFERENCES families(id) ON DELETE CASCADE,
    name        VARCHAR(100)    NOT NULL,
    created_by  UUID            NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_shopping_lists_family_id ON shopping_lists(family_id);

CREATE TABLE shopping_items (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    list_id     UUID            NOT NULL REFERENCES shopping_lists(id) ON DELETE CASCADE,
    family_id   UUID            NOT NULL REFERENCES families(id) ON DELETE CASCADE,
    name        VARCHAR(200)    NOT NULL,
    category    VARCHAR(50)     NOT NULL DEFAULT 'other'
                                CHECK (category IN (
                                    'produce', 'dairy', 'meat', 'bakery',
                                    'frozen', 'household', 'personal_care',
                                    'beverages', 'snacks', 'other'
                                )),
    quantity    VARCHAR(50),                        -- e.g. "2", "500g", "1 dozen"
    is_checked  BOOLEAN         NOT NULL DEFAULT FALSE,
    checked_by  UUID            REFERENCES profiles(id) ON DELETE SET NULL,
    checked_at  TIMESTAMPTZ,
    created_by  UUID            NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_shopping_items_list_id   ON shopping_items(list_id);
CREATE INDEX idx_shopping_items_family_id ON shopping_items(family_id);

-- !Downs

DROP TABLE IF EXISTS shopping_items;
DROP TABLE IF EXISTS shopping_lists;
```

---

### `conf/evolutions/default/5.sql` — Rewards

```sql
-- !Ups

CREATE TABLE rewards (
    id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    family_id    UUID            NOT NULL REFERENCES families(id) ON DELETE CASCADE,
    member_id    UUID            NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    title        VARCHAR(200)    NOT NULL,         -- e.g. "Movie night"
    points_cost  INTEGER         NOT NULL CHECK (points_cost > 0),
    status       VARCHAR(20)     NOT NULL DEFAULT 'active'
                                 CHECK (status IN ('active', 'pending_approval', 'approved', 'redeemed')),
    requested_at TIMESTAMPTZ,
    approved_by  UUID            REFERENCES profiles(id) ON DELETE SET NULL,
    approved_at  TIMESTAMPTZ,
    created_at   TIMESTAMPTZ     NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ     NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_rewards_family_id  ON rewards(family_id);
CREATE INDEX idx_rewards_member_id  ON rewards(member_id);
CREATE INDEX idx_rewards_status     ON rewards(family_id, status);

-- !Downs

DROP TABLE IF EXISTS rewards;
```

---

## 3. Table Reference Summary

### `families`
| Column | Type | Notes |
|---|---|---|
| id | UUID PK | Auto-generated |
| name | VARCHAR(100) | e.g. "The Smiths" |
| invite_code | CHAR(6) UNIQUE | e.g. "DOTO4X" — generated on creation |
| created_at | TIMESTAMPTZ | |
| updated_at | TIMESTAMPTZ | |

---

### `profiles`
| Column | Type | Notes |
|---|---|---|
| id | UUID PK | |
| family_id | UUID FK → families | NULL until they join/create a family |
| username | VARCHAR(50) UNIQUE | NULL for child profiles |
| password_hash | VARCHAR(255) | NULL for child profiles |
| display_name | VARCHAR(100) | Shown in UI, e.g. "Mum", "Jake" |
| role | VARCHAR(10) | `parent` or `child` |
| color | VARCHAR(7) | Hex colour for avatar, e.g. `#FF6B6B` |
| points | INTEGER | Running total, incremented on task completion |
| is_auth_account | BOOLEAN | FALSE for child profiles added by a parent |
| created_at | TIMESTAMPTZ | |
| updated_at | TIMESTAMPTZ | |

---

### `events`
| Column | Type | Notes |
|---|---|---|
| id | UUID PK | |
| family_id | UUID FK → families | |
| title | VARCHAR(200) | |
| description | TEXT | Nullable |
| start_at | TIMESTAMPTZ | |
| end_at | TIMESTAMPTZ | Must be after start_at (enforced by CHECK) |
| location | VARCHAR(300) | Nullable |
| color | VARCHAR(7) | Nullable — falls back to assignee colour in UI |
| assigned_to | UUID[] | Array of profile IDs — can be multiple members |
| created_by | UUID FK → profiles | |
| created_at | TIMESTAMPTZ | |
| updated_at | TIMESTAMPTZ | |

---

### `tasks`
| Column | Type | Notes |
|---|---|---|
| id | UUID PK | |
| family_id | UUID FK → families | |
| title | VARCHAR(200) | |
| description | TEXT | Nullable |
| assigned_to | UUID FK → profiles | Nullable — unassigned tasks allowed |
| status | VARCHAR(20) | `todo` / `in_progress` / `done` / `cancelled` |
| priority | VARCHAR(10) | `low` / `medium` / `high` |
| points | INTEGER | Points earned on completion, default 0 |
| due_at | TIMESTAMPTZ | Nullable |
| completed_at | TIMESTAMPTZ | Set when status → `done` |
| created_by | UUID FK → profiles | |
| created_at | TIMESTAMPTZ | |
| updated_at | TIMESTAMPTZ | |

---

### `shopping_lists`
| Column | Type | Notes |
|---|---|---|
| id | UUID PK | |
| family_id | UUID FK → families | |
| name | VARCHAR(100) | e.g. "Groceries", "Costco", "Hardware" |
| created_by | UUID FK → profiles | |
| created_at | TIMESTAMPTZ | |
| updated_at | TIMESTAMPTZ | |

---

### `shopping_items`
| Column | Type | Notes |
|---|---|---|
| id | UUID PK | |
| list_id | UUID FK → shopping_lists | |
| family_id | UUID FK → families | Denormalised for fast family-scoped queries |
| name | VARCHAR(200) | e.g. "Whole milk" |
| category | VARCHAR(50) | One of 9 enum values (see evolution) |
| quantity | VARCHAR(50) | Nullable free text, e.g. "2 litres" |
| is_checked | BOOLEAN | Default FALSE |
| checked_by | UUID FK → profiles | Nullable — who checked it off |
| checked_at | TIMESTAMPTZ | Nullable |
| created_by | UUID FK → profiles | |
| created_at | TIMESTAMPTZ | |
| updated_at | TIMESTAMPTZ | |

---

### `rewards`
| Column | Type | Notes |
|---|---|---|
| id | UUID PK | |
| family_id | UUID FK → families | |
| member_id | UUID FK → profiles | The child who set the goal |
| title | VARCHAR(200) | e.g. "Movie night", "Extra screen time" |
| points_cost | INTEGER | Points needed to redeem |
| status | VARCHAR(20) | `active` → `pending_approval` → `approved` → `redeemed` |
| requested_at | TIMESTAMPTZ | When child tapped "Redeem" |
| approved_by | UUID FK → profiles | The parent who approved |
| approved_at | TIMESTAMPTZ | |
| created_at | TIMESTAMPTZ | |
| updated_at | TIMESTAMPTZ | |

---

## 4. Reward Status Flow

```
[active]  ──── child taps Redeem ────►  [pending_approval]
                                                │
                          parent approves ──────┘
                                ▼
                          [approved]
                                │
                 parent marks redeemed ──────►  [redeemed]
```

---

## 5. Points System

- Points are stored on `profiles.points` as a running integer total
- When a task with `points > 0` is marked `done`, the backend adds `task.points` to `profile.points` for the `assigned_to` profile
- Points are **never subtracted** when a reward is redeemed — the leaderboard is a cumulative score
- For MVP, there is no points transaction log (add in V2 if needed for history)

---

## 6. Invite Code Generation (Pseudo-code)

```
Generate 6 chars from: ABCDEFGHJKLMNPQRSTUVWXYZ23456789
  (excludes O, 0, I, 1 to avoid visual confusion)
Check uniqueness in families table
Retry up to 5 times if collision (astronomically unlikely)
Store on families.invite_code
```

---

## 7. Slick Column Mappings (Scala)

PostgreSQL types map to Scala types in Slick as follows:

| PostgreSQL | Scala / Slick |
|---|---|
| UUID | `java.util.UUID` |
| VARCHAR / TEXT | `String` |
| BOOLEAN | `Boolean` |
| INTEGER | `Int` |
| TIMESTAMPTZ | `java.time.Instant` |
| UUID[] | `List[UUID]` with custom Slick mapper |

The UUID array column (`events.assigned_to`) requires a custom Slick `MappedColumnType`:

```scala
implicit val uuidListMapper = MappedColumnType.base[List[UUID], String](
  list => list.map(_.toString).mkString(","),
  str  => if (str.isEmpty) List.empty
          else str.split(",").map(UUID.fromString).toList
)
```
