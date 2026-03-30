# --- !Ups

CREATE TABLE events (
    id          UUID         PRIMARY KEY DEFAULT gen_random_uuid(),
    family_id   UUID         NOT NULL REFERENCES families(id) ON DELETE CASCADE,
    title       VARCHAR(200) NOT NULL,
    description TEXT,
    start_at    TIMESTAMPTZ  NOT NULL,
    end_at      TIMESTAMPTZ  NOT NULL,
    location    VARCHAR(300),
    color       VARCHAR(7),
    assigned_to UUID[]       NOT NULL DEFAULT '{}',
    created_by  UUID         NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    created_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),
    updated_at  TIMESTAMPTZ  NOT NULL DEFAULT NOW(),

    CONSTRAINT events_end_after_start CHECK (end_at > start_at)
);

CREATE INDEX idx_events_family_id ON events(family_id);
CREATE INDEX idx_events_start_at  ON events(family_id, start_at);

# --- !Downs

DROP TABLE IF EXISTS events;
