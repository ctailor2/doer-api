INSERT INTO completed_todos (user_id, task, completed_at)
SELECT user_id, task, updated_at
FROM todos
WHERE completed = true;
