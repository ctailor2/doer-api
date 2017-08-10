CREATE TABLE list_unlocks (
    id integer NOT NULL,
    user_id integer NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL
);
CREATE SEQUENCE list_unlocks_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER SEQUENCE list_unlocks_id_seq OWNED BY list_unlocks.id;
ALTER TABLE ONLY list_unlocks ALTER COLUMN id SET DEFAULT nextval('list_unlocks_id_seq'::regclass);
ALTER TABLE ONLY list_unlocks
    ADD CONSTRAINT list_unlocks_pkey PRIMARY KEY (id);
ALTER TABLE ONLY list_unlocks
    ADD CONSTRAINT list_unlocks_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id);
CREATE INDEX index_list_unlocks_on_user_id ON list_unlocks USING btree (user_id);