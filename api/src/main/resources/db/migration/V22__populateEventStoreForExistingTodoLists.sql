-- Port over all existing todos to list events as deferred todos since this is the easiest way to produce the equivalent model
INSERT INTO list_events(user_id, list_id, event_class, version, data)
SELECT
    "userId",
    todo_added_events."listId",
    'com.doerapispring.domain.events.DeferredTodoAddedEvent',
    ROW_NUMBER() OVER (PARTITION BY "userId", todo_added_events."listId" ORDER BY position ASC) - 1,
    row_to_json(todo_added_events)
FROM (
    SELECT user_identifier AS "userId", list_id AS "listId", todos.uuid AS "todoId", task
    FROM todos JOIN lists ON todos.list_id = lists.uuid
) as todo_added_events
JOIN todos ON "todoId" = todos.uuid

-- And then add a pull event to re-establish the demarcation index and show users their todos
INSERT INTO list_events(user_id, list_id, event_class, version, data)
SELECT max(user_id) AS user_id, max(list_id) AS list_id, 'com.doerapispring.domain.events.PulledEvent', max(version) + 1, json_build_object('userId', user_id, 'listId', list_id)
FROM list_events
GROUP BY user_id, list_id