package com.doerapispring.storage;

import com.doerapispring.domain.*;
import org.flywaydb.core.api.configuration.Configuration;
import org.flywaydb.core.api.migration.Context;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.flyway.FlywayMigrationStrategy;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.time.Clock;
import java.time.Instant;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public class V23__PopulateEventStoreForExistingCompletedTodosTest {
    @Autowired
    private UserDAO userDAO;

    @Autowired
    private TodoListDao todoListDao;

    @Autowired
    private Clock clock;

    @Autowired
    private Configuration flywayConfiguration;

    @Autowired
    private DataSource dataSource;

    @Autowired
    private TodoListCommandModelEventSourcedRepository todoListCommandModelEventSourcedRepository;

    @Autowired
    private UserRepository userRepository;

    @TestConfiguration
    static class TestFlywayConfiguration {
        @Bean
        public FlywayMigrationStrategy flywayMigrationStrategy() {
            return flyway -> {
                // do nothing
            };
        }
    }

    private Context flywayContext;

    private TodoListCommandModelRepository todoListCommandModelRepository;

    private V23__PopulateEventStoreForExistingCompletedTodos migration = new V23__PopulateEventStoreForExistingCompletedTodos();

    private UserId userId = new UserId("someUserId");

    private ListId listId = new ListId("someListId");
    private TodoList todoList = new TodoList(userId, listId, "someListName", 0, Date.from(Instant.EPOCH));

    @Autowired
    private TodoListRepository todoListRepository;

    @Before
    public void setUp() throws Exception {
        userRepository.save(new User(userId, listId));
        todoListRepository.save(todoList);
        flywayContext = new Context() {
            @Override
            public Configuration getConfiguration() {
                return flywayConfiguration;
            }

            @Override
            public Connection getConnection() {
                try {
                    return dataSource.getConnection();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        };
    }

    @Autowired
    private CompletedTodoRepository completedTodoRepository;

    @Autowired
    private CompletedTodoListEventSourcedRepository completedTodoListEventSourcedRepository;

    @Test
    public void shouldNotReimportCompletedTodosWhoseEventsAreAlreadyPopulated() throws Exception {
        TodoListCommandModel todoListCommandModel = TodoListCommandModel.newInstance(clock, todoList);
        TodoId todoId = new TodoId("someId");
        String task = "existingCompletedTask";
        todoListCommandModel.add(todoId, task);
        todoListCommandModel.complete(todoId);
        todoListCommandModelEventSourcedRepository.save(todoListCommandModel);

        CompletedTodoId completedTodoId = new CompletedTodoId(todoId.getIdentifier());
        Date completedAt = Date.from(Instant.now());
        completedTodoRepository.save(
                new CompletedTodoWriteModel(
                        userId,
                        listId,
                        completedTodoId,
                        task,
                        completedAt));

        migration.migrate(flywayContext);

        CompletedTodoList completedTodoList = completedTodoListEventSourcedRepository.find(userId, listId).get();

        assertThat(completedTodoList.getTodos()).usingElementComparatorIgnoringFields("completedAt")
                .containsExactly(new CompletedTodoReadModel(completedTodoId, task, completedAt));

        assertThat(todoListCommandModelEventSourcedRepository.find(userId, listId)).isPresent();
    }

    @Test
    public void existingCompletedTodosHaveTheirAppropriateEventsPopulated() {
        CompletedTodoId completedTodoId = new CompletedTodoId("someId");
        String task = "someTask";
        Date completedAt = Date.from(Instant.now());
        completedTodoRepository.save(
                new CompletedTodoWriteModel(
                        userId,
                        listId,
                        completedTodoId,
                        task,
                        completedAt));

        migration.migrate(flywayContext);

        CompletedTodoList completedTodoList = completedTodoListEventSourcedRepository.find(userId, listId).get();

        assertThat(completedTodoList.getTodos()).contains(new CompletedTodoReadModel(completedTodoId, task, completedAt));

        assertThat(todoListCommandModelEventSourcedRepository.find(userId, listId)).isPresent();
    }

    @Test
    public void startsVersioningTheCompletedTodoEventsAfterTheLastEventVersion() throws Exception {
        TodoListCommandModel todoListCommandModel = TodoListCommandModel.newInstance(clock, todoList);
        todoListCommandModel.add(new TodoId("id1"), "task1");
        todoListCommandModel.add(new TodoId("id2"), "task2");
        todoListCommandModelEventSourcedRepository.save(todoListCommandModel);
        assertThat(todoListCommandModelEventSourcedRepository.find(userId, listId).get().getAllTodos()).hasSize(2);

        CompletedTodoId completedTodoId = new CompletedTodoId("someId");
        String task = "someTask";
        Date completedAt = Date.from(Instant.now());
        completedTodoRepository.save(
                new CompletedTodoWriteModel(
                        userId,
                        listId,
                        completedTodoId,
                        task,
                        completedAt));
        assertThat(todoListCommandModelEventSourcedRepository.find(userId, listId).get().getAllTodos()).hasSize(2);

        migration.migrate(flywayContext);

        CompletedTodoList completedTodoList = completedTodoListEventSourcedRepository.find(userId, listId).get();

        assertThat(completedTodoList.getTodos()).contains(new CompletedTodoReadModel(completedTodoId, task, completedAt));

        assertThat(todoListCommandModelEventSourcedRepository.find(userId, listId)).isPresent();
    }
}