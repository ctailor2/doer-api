CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE completed_todos (
    uuid character varying NOT NULL default uuid_generate_v4(),
    user_id integer NOT NULL,
    task character varying NOT NULL,
    completed_at timestamp without time zone NOT NULL
);
ALTER TABLE ONLY completed_todos
    ADD CONSTRAINT completed_todos_pkey PRIMARY KEY (uuid);
ALTER TABLE ONLY completed_todos
    ADD CONSTRAINT completed_todos_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id);
CREATE INDEX index_completed_todos_on_user_id ON completed_todos USING btree (user_id);