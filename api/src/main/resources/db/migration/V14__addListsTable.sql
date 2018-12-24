CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE lists AS
    SELECT
        uuid_generate_v4()::varchar AS uuid,
        'default'::varchar AS name,
        id AS user_id,
        last_unlocked_at,
        demarcation_index
    FROM users;

ALTER TABLE lists ADD FOREIGN KEY (user_id) REFERENCES users(id);
CREATE UNIQUE INDEX index_lists_on_uuid ON lists (uuid);

ALTER TABLE lists ALTER COLUMN uuid SET NOT NULL;
ALTER TABLE lists ALTER COLUMN name SET NOT NULL;
ALTER TABLE lists ALTER COLUMN user_id SET NOT NULL;
ALTER TABLE lists ALTER COLUMN last_unlocked_at SET NOT NULL;
ALTER TABLE lists ALTER COLUMN demarcation_index SET NOT NULL;

ALTER TABLE todos ADD COLUMN list_id character varying;

UPDATE todos
SET list_id = lists.uuid
FROM lists
WHERE lists.user_id = todos.user_id;

ALTER TABLE todos ALTER COLUMN list_id SET NOT NULL;
ALTER TABLE todos ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE todos ADD FOREIGN KEY (list_id) REFERENCES lists(uuid);
ALTER TABLE todos DROP CONSTRAINT todos_user_id_fkey;

