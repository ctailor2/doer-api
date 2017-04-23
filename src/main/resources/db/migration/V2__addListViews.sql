CREATE TABLE list_views (
    id integer NOT NULL,
    user_id integer NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL
);
CREATE SEQUENCE list_views_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER SEQUENCE list_views_id_seq OWNED BY list_views.id;
ALTER TABLE ONLY list_views ALTER COLUMN id SET DEFAULT nextval('list_views_id_seq'::regclass);
ALTER TABLE ONLY list_views
    ADD CONSTRAINT list_views_pkey PRIMARY KEY (id);
ALTER TABLE ONLY list_views
    ADD CONSTRAINT list_views_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id);
CREATE INDEX index_list_views_on_user_id ON list_views USING btree (user_id);