TRUNCATE TABLE todo_lists;

INSERT INTO todo_lists
SELECT
    user_identifier AS user_id,
    uuid AS list_id,
    json_build_object(
        'listId', json_build_object('name', uuid),
        'profileName', name,
        'todos', json_build_array(),
        'completedTodos', json_build_array(),
        'lastUnlockedAt', 0,
        'demarcationIndex', 0,
        'sectionName', 'now',
        'deferredSectionName', 'later') AS data,
    TIMESTAMP 'epoch' AS created_at
FROM lists;