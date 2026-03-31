# --- !Ups

ALTER TABLE profiles ALTER COLUMN username SET NOT NULL;
ALTER TABLE profiles ALTER COLUMN password_hash SET NOT NULL;
ALTER TABLE profiles ADD COLUMN IF NOT EXISTS streak           INTEGER NOT NULL DEFAULT 0;
ALTER TABLE profiles ADD COLUMN IF NOT EXISTS last_streak_date DATE;

# --- !Downs

ALTER TABLE profiles ALTER COLUMN username    DROP NOT NULL;
ALTER TABLE profiles ALTER COLUMN password_hash DROP NOT NULL;
ALTER TABLE profiles DROP COLUMN IF EXISTS streak;
ALTER TABLE profiles DROP COLUMN IF EXISTS last_streak_date;
