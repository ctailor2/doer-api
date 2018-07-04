CREATE TABLE completed_todos (
    id integer NOT NULL,
    user_id integer NOT NULL,
    task character varying NOT NULL,
    completed_at timestamp without time zone NOT NULL
);
CREATE SEQUENCE completed_todos_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER SEQUENCE completed_todos_id_seq OWNED BY completed_todos.id;
ALTER TABLE ONLY completed_todos ALTER COLUMN id SET DEFAULT nextval('completed_todos_id_seq'::regclass);
ALTER TABLE ONLY completed_todos
    ADD CONSTRAINT completed_todos_pkey PRIMARY KEY (id);
ALTER TABLE ONLY completed_todos
    ADD CONSTRAINT completed_todos_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id);
CREATE INDEX index_completed_todos_on_user_id ON completed_todos USING btree (user_id);