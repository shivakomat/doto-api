# --- !Ups

ALTER TABLE profiles ALTER COLUMN username      DROP NOT NULL;
ALTER TABLE profiles ALTER COLUMN password_hash DROP NOT NULL;

# --- !Downs

ALTER TABLE profiles ALTER COLUMN username      SET NOT NULL;
ALTER TABLE profiles ALTER COLUMN password_hash SET NOT NULL;
