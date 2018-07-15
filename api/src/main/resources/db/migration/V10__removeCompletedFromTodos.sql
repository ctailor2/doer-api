DELETE FROM todos
WHERE completed = true;

ALTER TABLE todos
DROP COLUMN completed;
