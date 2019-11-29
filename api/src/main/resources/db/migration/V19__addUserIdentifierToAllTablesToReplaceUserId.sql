-- session tokens
ALTER TABLE session_tokens ADD COLUMN user_identifier character varying;
ALTER TABLE session_tokens ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE session_tokens DROP CONSTRAINT session_tokens_user_id_fkey;

UPDATE session_tokens
SET user_identifier = users.email
FROM users
WHERE session_tokens.user_id = users.id;

ALTER TABLE session_tokens ALTER COLUMN user_identifier SET NOT NULL;
ALTER TABLE session_tokens ADD FOREIGN KEY (user_identifier) REFERENCES users(email);

-- lists
ALTER TABLE lists ADD COLUMN user_identifier character varying;
ALTER TABLE lists ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE lists DROP CONSTRAINT lists_user_id_fkey;

UPDATE lists
SET user_identifier = users.email
FROM users
WHERE lists.user_id = users.id;

ALTER TABLE lists ALTER COLUMN user_identifier SET NOT NULL;
ALTER TABLE lists ADD FOREIGN KEY (user_identifier) REFERENCES users(email);

-- completed todos
ALTER TABLE completed_todos ADD COLUMN user_identifier character varying;
ALTER TABLE completed_todos ALTER COLUMN user_id DROP NOT NULL;
ALTER TABLE completed_todos DROP CONSTRAINT completed_todos_user_id_fkey;

UPDATE completed_todos
SET user_identifier = users.email
FROM users
WHERE completed_todos.user_id = users.id;

ALTER TABLE completed_todos ALTER COLUMN user_identifier SET NOT NULL;
ALTER TABLE completed_todos ADD FOREIGN KEY (user_identifier) REFERENCES users(email);
