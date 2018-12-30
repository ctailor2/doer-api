ALTER TABLE completed_todos ADD COLUMN list_id character varying;

UPDATE completed_todos
SET list_id = lists.uuid
FROM lists
WHERE lists.user_id = completed_todos.user_id;

ALTER TABLE completed_todos ALTER COLUMN list_id SET NOT NULL;
ALTER TABLE completed_todos ADD FOREIGN KEY (list_id) REFERENCES lists(uuid);
