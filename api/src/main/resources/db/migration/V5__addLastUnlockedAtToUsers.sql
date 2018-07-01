ALTER TABLE users
ADD COLUMN last_unlocked_at timestamp without time zone NOT NULL default timestamp 'epoch';