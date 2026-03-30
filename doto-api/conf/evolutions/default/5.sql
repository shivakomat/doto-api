# --- !Ups

CREATE TABLE rewards (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    family_id    UUID        NOT NULL REFERENCES families(id) ON DELETE CASCADE,
    member_id    UUID        NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    title        VARCHAR(200) NOT NULL,
    points_cost  INTEGER     NOT NULL CHECK (points_cost > 0),
    status       VARCHAR(20) NOT NULL DEFAULT 'active'
                             CHECK (status IN ('active', 'pending_approval', 'approved', 'redeemed')),
    requested_at TIMESTAMPTZ,
    approved_by  UUID        REFERENCES profiles(id) ON DELETE SET NULL,
    approved_at  TIMESTAMPTZ,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_rewards_family_id ON rewards(family_id);
CREATE INDEX idx_rewards_member_id ON rewards(member_id);
CREATE INDEX idx_rewards_status    ON rewards(family_id, status);

# --- !Downs

DROP TABLE IF EXISTS rewards;
