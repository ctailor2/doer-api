package com.doerapispring;

import com.doerapispring.domain.*;
import com.doerapispring.storage.CompletedTodoListEventSourcedRepository;
import com.doerapispring.storage.DeprecatedTodoListModelEventSourcedRepository;
import com.doerapispring.storage.DeprecatedTodoListModelSnapshotRepository;
import com.doerapispring.storage.TodoListModelSnapshotRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.Instant;
import java.util.Date;

import static scala.jdk.javaapi.CollectionConverters.asScala;

@SpringBootApplication
public class Migrator implements CommandLineRunner {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private ObjectMapper objectMapper;

    public static void main(String[] args) {
        SpringApplication.run(Migrator.class, args);
    }

    @Override
    public void run(String... args) {
        DeprecatedTodoListModelSnapshotRepository deprecatedSnapshotRepo = new DeprecatedTodoListModelSnapshotRepository(jdbcTemplate, objectMapper);
        DeprecatedTodoListModelEventSourcedRepository deprecatedRepo = new DeprecatedTodoListModelEventSourcedRepository(deprecatedSnapshotRepo, objectMapper, jdbcTemplate);
        CompletedTodoListEventSourcedRepository completedRepo = new CompletedTodoListEventSourcedRepository(objectMapper, jdbcTemplate);
        TodoListModelSnapshotRepository snapshotRepo = new TodoListModelSnapshotRepository(jdbcTemplate, objectMapper);
        jdbcTemplate.query("select user_id, list_id from todo_lists", (resultSet) -> {
            UserId userId = new UserId(resultSet.getString("user_id"));
            ListId listId = new ListId(resultSet.getString("list_id"));
            CompletedTodoList completedTodoList = completedRepo.find(userId, listId).get();
            DeprecatedTodoListModel deprecatedTodoListModel = deprecatedRepo.find(userId, listId).get();
            TodoListModel todoListModel = new TodoListModel(
                    deprecatedTodoListModel.todos()
                            .map(deprecatedTodo -> new Todo(deprecatedTodo.getTask())),
                    asScala(completedTodoList.getTodos()).toList()
                            .map(deprecatedCompletedTodo -> new CompletedTodo(
                                    deprecatedCompletedTodo.getTask(),
                                    deprecatedCompletedTodo.getCompletedAt())),
                    deprecatedTodoListModel.lastUnlockedAt(),
                    deprecatedTodoListModel.demarcationIndex());
            snapshotRepo.save(userId, listId, new Snapshot<>(todoListModel, Date.from(Instant.now())));
        });
    }
}
