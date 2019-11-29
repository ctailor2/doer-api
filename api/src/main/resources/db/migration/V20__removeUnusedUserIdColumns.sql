-- session tokens
ALTER TABLE session_tokens DROP COLUMN user_id;

-- lists
ALTER TABLE lists DROP COLUMN user_id;

-- completed todos
ALTER TABLE completed_todos DROP COLUMN user_id;
