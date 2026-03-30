# --- !Ups

CREATE EXTENSION IF NOT EXISTS "pgcrypto";

CREATE TABLE families (
    id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    name        VARCHAR(100) NOT NULL,
    invite_code CHAR(6)      NOT NULL UNIQUE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_families_invite_code ON families(invite_code);

CREATE TABLE profiles (
    id              UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    family_id       UUID        REFERENCES families(id) ON DELETE SET NULL,
    username        VARCHAR(50) UNIQUE,
    password_hash   VARCHAR(255),
    display_name    VARCHAR(100) NOT NULL,
    role            VARCHAR(10)  NOT NULL CHECK (role IN ('parent', 'child')),
    color           VARCHAR(7)   NOT NULL DEFAULT '#6C63FF',
    points          INTEGER      NOT NULL DEFAULT 0,
    is_auth_account BOOLEAN      NOT NULL DEFAULT TRUE,
    created_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at      TIMESTAMPTZ  NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_profiles_family_id ON profiles(family_id);
CREATE INDEX idx_profiles_username  ON profiles(username);

# --- !Downs

DROP TABLE IF EXISTS profiles;
DROP TABLE IF EXISTS families;
