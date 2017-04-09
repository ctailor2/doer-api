CREATE TABLE users (
    id integer NOT NULL,
    email character varying NOT NULL,
    password_digest character varying NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    timezone character varying DEFAULT 'Etc/UTC'::character varying NOT NULL
);
CREATE SEQUENCE users_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER SEQUENCE users_id_seq OWNED BY users.id;
ALTER TABLE ONLY users ALTER COLUMN id SET DEFAULT nextval('users_id_seq'::regclass);
ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


CREATE TABLE session_tokens (
    id integer NOT NULL,
    user_id integer NOT NULL,
    token character varying NOT NULL,
    expires_at timestamp without time zone NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL
);
CREATE SEQUENCE session_tokens_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER SEQUENCE session_tokens_id_seq OWNED BY session_tokens.id;
ALTER TABLE ONLY session_tokens ALTER COLUMN id SET DEFAULT nextval('session_tokens_id_seq'::regclass);
ALTER TABLE ONLY session_tokens
    ADD CONSTRAINT session_tokens_pkey PRIMARY KEY (id);
CREATE UNIQUE INDEX index_session_tokens_on_token ON session_tokens USING btree (token);
CREATE INDEX index_session_tokens_on_user_id ON session_tokens USING btree (user_id);
ALTER TABLE ONLY session_tokens
    ADD CONSTRAINT session_tokens_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id);


CREATE TABLE todos (
    id integer NOT NULL,
    user_id integer NOT NULL,
    task character varying NOT NULL,
    created_at timestamp without time zone NOT NULL,
    updated_at timestamp without time zone NOT NULL,
    "position" integer,
    completed boolean DEFAULT false NOT NULL,
    active boolean DEFAULT false NOT NULL
);
CREATE SEQUENCE todos_id_seq
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;
ALTER SEQUENCE todos_id_seq OWNED BY todos.id;
ALTER TABLE ONLY todos ALTER COLUMN id SET DEFAULT nextval('todos_id_seq'::regclass);
ALTER TABLE ONLY todos
    ADD CONSTRAINT todos_pkey PRIMARY KEY (id);
ALTER TABLE ONLY todos
    ADD CONSTRAINT todos_user_id_fkey FOREIGN KEY (user_id) REFERENCES users(id);
CREATE INDEX index_todos_on_user_id ON todos USING btree (user_id);
CREATE UNIQUE INDEX index_users_on_email ON users USING btree (email);