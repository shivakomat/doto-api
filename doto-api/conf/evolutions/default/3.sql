# --- !Ups

CREATE TABLE tasks (
    id           UUID        PRIMARY KEY DEFAULT gen_random_uuid(),
    family_id    UUID        NOT NULL REFERENCES families(id) ON DELETE CASCADE,
    title        VARCHAR(200) NOT NULL,
    description  TEXT,
    assigned_to  UUID        REFERENCES profiles(id) ON DELETE SET NULL,
    status       VARCHAR(20) NOT NULL DEFAULT 'todo'
                             CHECK (status IN ('todo', 'in_progress', 'done', 'cancelled')),
    priority     VARCHAR(10) NOT NULL DEFAULT 'medium'
                             CHECK (priority IN ('low', 'medium', 'high')),
    points       INTEGER     NOT NULL DEFAULT 0 CHECK (points >= 0),
    due_at       TIMESTAMPTZ,
    completed_at TIMESTAMPTZ,
    created_by   UUID        NOT NULL REFERENCES profiles(id) ON DELETE CASCADE,
    created_at   TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at   TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_tasks_family_id   ON tasks(family_id);
CREATE INDEX idx_tasks_assigned_to ON tasks(assigned_to);
CREATE INDEX idx_tasks_status      ON tasks(family_id, status);

# --- !Downs

DROP TABLE IF EXISTS tasks;
