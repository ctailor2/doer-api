CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

ALTER TABLE todos
DROP COLUMN id;

ALTER TABLE todos ADD COLUMN uuid character varying NOT NULL default uuid_generate_v4();
CREATE UNIQUE INDEX index_todos_on_uuid ON todos USING btree (uuid);