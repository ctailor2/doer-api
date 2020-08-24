CREATE TABLE todo_lists (
    user_id varchar NOT NULL,
    list_id varchar NOT NULL,
    data varchar NOT NULL,
    created_at timestamp without time zone NOT NULL
);

CREATE UNIQUE INDEX todo_lists_index ON todo_lists (user_id, list_id);

INSERT INTO todo_lists
SELECT
    user_identifier AS user_id,
    uuid AS list_id,
    json_build_object(
        'listId', json_build_object('name', uuid),
        'profileName', name,
        'todos', json_build_array(),
        'lastUnlockedAt', 0,
        'demarcationIndex', 0) AS data,
    TIMESTAMP 'epoch' AS created_at
FROM lists;