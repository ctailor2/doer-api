ALTER TABLE list_events ADD COLUMN created_at timestamp without time zone NOT NULL default CURRENT_TIMESTAMP;

CREATE TEMPORARY TABLE todo_completed_events AS
SELECT user_id, list_id, version, todo_id, completed_at_text::timestamp AS completed_at
FROM (
    SELECT user_id, list_id, version, data::json->>'completedTodoId' AS todo_id, data::json->>'completedAt' AS completed_at_text
    FROM list_events
    WHERE event_class = 'com.doerapispring.domain.events.TodoCompletedEvent'
) AS sub_todo_completed_events
WHERE completed_at_text ~ '\d{2}-\d{2}-\d{2}.+'
UNION
SELECT user_id, list_id, version, todo_id, to_timestamp(completed_at_text::bigint::double precision / 1000) AS completed_at
FROM (
    SELECT user_id, list_id, version, data::json->>'completedTodoId' AS todo_id, data::json->>'completedAt' AS completed_at_text
    FROM list_events
    WHERE event_class = 'com.doerapispring.domain.events.TodoCompletedEvent'
) AS sub_todo_completed_events
WHERE completed_at_text !~ '\d{2}-\d{2}-\d{2}.+';

UPDATE list_events
SET created_at = todo_completed_events.completed_at
FROM todo_completed_events
WHERE list_events.user_id = todo_completed_events.user_id
AND list_events.list_id = todo_completed_events.list_id
AND list_events.version = todo_completed_events.version;