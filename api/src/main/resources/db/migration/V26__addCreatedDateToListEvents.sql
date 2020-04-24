ALTER TABLE list_events ADD COLUMN created_at timestamp without time zone NOT NULL default CURRENT_TIMESTAMP;

CREATE TEMPORARY TABLE todo_completed_events AS
SELECT user_id, list_id, version, data::json->>'completedTodoId' AS todo_id, (data::json->>'completedAt')::timestamp AS completed_at
FROM list_events
WHERE event_class = 'com.doerapispring.domain.events.TodoCompletedEvent';

UPDATE list_events
SET created_at = todo_completed_events.completed_at
FROM todo_completed_events
WHERE list_events.user_id = todo_completed_events.user_id
AND list_events.list_id = todo_completed_events.list_id
AND list_events.version = todo_completed_events.version;