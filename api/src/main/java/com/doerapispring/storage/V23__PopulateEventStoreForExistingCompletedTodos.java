package com.doerapispring.storage;

import org.flywaydb.core.api.migration.BaseJavaMigration;
import org.flywaydb.core.api.migration.Context;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.SingleConnectionDataSource;

public class V23__PopulateEventStoreForExistingCompletedTodos extends BaseJavaMigration {
    @Override
    public void migrate(Context context) {
        JdbcTemplate jdbcTemplate = new JdbcTemplate(new SingleConnectionDataSource(context.getConnection(), true));
        jdbcTemplate.execute(
                "INSERT INTO list_events(user_id, list_id, event_class, version, data)\n" +
                        "SELECT\n" +
                        "    \"userId\",\n" +
                        "    \"listId\",\n" +
                        "    event_class,\n" +
                        "    ROW_NUMBER() OVER (PARTITION BY \"userId\", \"listId\" ORDER BY \"completedAt\", event_class ASC) + max_version AS version,\n" +
                        "    data\n" +
                        "FROM (\n" +
                        "    SELECT\n" +
                        "        \"userId\",\n" +
                        "        \"listId\",\n" +
                        "        event_class,\n" +
                        "        data,\n" +
                        "        \"completedAt\",\n" +
                        "        CASE WHEN max_version IS NULL THEN 0 ELSE max_version END AS max_version\n" +
                        "    FROM (\n" +
                        "        SELECT\n" +
                        "            \"userId\",\n" +
                        "            \"listId\",\n" +
                        "            'com.doerapispring.domain.events.TodoCompletedEvent' AS event_class,\n" +
                        "            row_to_json(completedTodosAsEvents) AS data,\n" +
                        "            \"completedAt\"\n" +
                        "        FROM (\n" +
                        "            SELECT\n" +
                        "                user_identifier AS \"userId\",\n" +
                        "                completed_todos.list_id AS \"listId\",\n" +
                        "                uuid AS \"completedTodoId\",\n" +
                        "                completed_at AS \"completedAt\",\n" +
                        "                task\n" +
                        "            FROM completed_todos\n" +
                        "            LEFT JOIN (\n" +
                        "                SELECT user_id, list_id, data::json->>'completedTodoId' AS todo_id\n" +
                        "                FROM list_events\n" +
                        "                WHERE event_class = 'com.doerapispring.domain.events.TodoCompletedEvent'\n" +
                        "            ) AS completed_events\n" +
                        "            ON completed_todos.user_identifier = completed_events.user_id AND completed_todos.list_id = completed_events.list_id AND completed_todos.uuid = completed_events.todo_id\n" +
                        "            WHERE todo_id IS NULL\n" +
                        "        ) AS completedTodosAsEvents\n" +
                        "        UNION ALL\n" +
                        "        SELECT\n" +
                        "            \"userId\",\n" +
                        "            \"listId\",\n" +
                        "            'com.doerapispring.domain.events.DeferredTodoAddedEvent' AS event_class,\n" +
                        "            json_build_object('userId', \"userId\", 'listId', \"listId\", 'todoId', \"todoId\", 'task', task) AS data,\n" +
                        "            \"completedAt\"\n" +
                        "        FROM (\n" +
                        "            SELECT\n" +
                        "                user_identifier \"userId\",\n" +
                        "                completed_todos.list_id \"listId\",\n" +
                        "                uuid \"todoId\",\n" +
                        "                completed_at AS \"completedAt\",\n" +
                        "                task\n" +
                        "            FROM completed_todos\n" +
                        "            LEFT JOIN (\n" +
                        "                SELECT user_id, list_id, data::json->>'completedTodoId' AS todo_id\n" +
                        "                FROM list_events\n" +
                        "                WHERE event_class = 'com.doerapispring.domain.events.TodoCompletedEvent'\n" +
                        "            ) AS completed_events\n" +
                        "            ON completed_todos.user_identifier = completed_events.user_id AND completed_todos.list_id = completed_events.list_id AND completed_todos.uuid = completed_events.todo_id\n" +
                        "            WHERE todo_id IS NULL\n" +
                        "        ) AS completedTodosAsEvents\n" +
                        "    ) AS allCompletedTodoEvents\n" +
                        "    LEFT JOIN (\n" +
                        "        SELECT user_id, list_id, max(version) AS max_version\n" +
                        "        FROM list_events\n" +
                        "        GROUP BY user_id, list_id\n" +
                        "    ) AS currentEvents\n" +
                        "    ON allCompletedTodoEvents.\"userId\" = currentEvents.user_id AND allCompletedTodoEvents.\"listId\" = currentEvents.list_id\n" +
                        ") AS allCompletedTodoEventsWithMaxVersion;");
    }
}