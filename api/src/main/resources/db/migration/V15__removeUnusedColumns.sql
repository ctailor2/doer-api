ALTER TABLE users
DROP COLUMN last_unlocked_at,
DROP COLUMN demarcation_index;

ALTER TABLE todos
DROP COLUMN user_id;