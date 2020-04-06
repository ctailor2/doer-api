INSERT INTO list_events(user_id, list_id, event_class, version, data)
SELECT
    "userId",
    "listId",
    event_class,
    ROW_NUMBER() OVER (PARTITION BY "userId", "listId" ORDER BY "completedAt", event_class ASC) + max_version AS version,
    data
FROM (
    SELECT
        "userId",
        "listId",
        event_class,
        data,
        "completedAt",
        CASE WHEN max_version IS NULL THEN 0 ELSE max_version END AS max_version
    FROM (
        SELECT
            "userId",
            "listId",
            'com.doerapispring.domain.events.TodoCompletedEvent' AS event_class,
            row_to_json(completedTodosAsEvents) AS data,
            "completedAt"
        FROM (
            SELECT
                user_identifier AS "userId",
                completed_todos.list_id AS "listId",
                uuid AS "completedTodoId",
                completed_at AS "completedAt",
                task
            FROM completed_todos
            LEFT JOIN (
                SELECT user_id, list_id, data::json->>'completedTodoId' AS todo_id
                FROM list_events
                WHERE event_class = 'com.doerapispring.domain.events.TodoCompletedEvent'
            ) AS completed_events
            ON completed_todos.user_identifier = completed_events.user_id AND completed_todos.list_id = completed_events.list_id AND completed_todos.uuid = completed_events.todo_id
            WHERE todo_id IS NULL
        ) AS completedTodosAsEvents
        UNION ALL
        SELECT
            "userId",
            "listId",
            'com.doerapispring.domain.events.DeferredTodoAddedEvent' AS event_class,
            json_build_object('userId', "userId", 'listId', "listId", 'todoId', "todoId", 'task', task) AS data,
            "completedAt"
        FROM (
            SELECT
                user_identifier "userId",
                completed_todos.list_id "listId",
                uuid "todoId",
                completed_at AS "completedAt",
                task
            FROM completed_todos
            LEFT JOIN (
                SELECT user_id, list_id, data::json->>'completedTodoId' AS todo_id
                FROM list_events
                WHERE event_class = 'com.doerapispring.domain.events.TodoCompletedEvent'
            ) AS completed_events
            ON completed_todos.user_identifier = completed_events.user_id AND completed_todos.list_id = completed_events.list_id AND completed_todos.uuid = completed_events.todo_id
            WHERE todo_id IS NULL
        ) AS completedTodosAsEvents
    ) AS allCompletedTodoEvents
    LEFT JOIN (
        SELECT user_id, list_id, max(version) AS max_version
        FROM list_events
        GROUP BY user_id, list_id
    ) AS currentEvents
    ON allCompletedTodoEvents."userId" = currentEvents.user_id AND allCompletedTodoEvents."listId" = currentEvents.list_id
) AS allCompletedTodoEventsWithMaxVersion;