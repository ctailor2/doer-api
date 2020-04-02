package com.doerapispring.storage;

import com.doerapispring.domain.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@ActiveProfiles(value = "test")
@RunWith(SpringRunner.class)
public class TodoListCommandModelRepositoryTest {
    private TodoListCommandModelRepository todoListCommandModelRepository;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Autowired
    private TodoListDao todoListDao;

    @Autowired
    private UserDAO userDao;

    private Clock clock;
    private UserRepository userRepository;

    @Before
    public void setUp() throws Exception {
        clock = Clock.systemDefaultZone();
        todoListCommandModelRepository = new TodoListCommandModelRepository(userDao, todoListDao, clock);
        userRepository = new UserRepository(userDao);
    }

    @Test
    public void savesTodoList() throws Exception {
        UserId userId = new UserId("someIdentifier");
        ListId listId = new ListId("someListIdentifier");
        userRepository.save(new User(userId, listId));
        TodoListCommandModel todoListCommandModel = new TodoListCommandModel(clock, userId, listId, "someName", Date.from(Instant.parse("2007-12-03T10:15:30.00Z")), new ArrayList<>(), 0);
        todoListCommandModel.addDeferred(new TodoId("1"), "firstTask");
        todoListCommandModel.add(new TodoId("2"), "secondTask");
        todoListCommandModel.addDeferred(new TodoId("3"), "thirdTask");
        todoListCommandModel.add(new TodoId("4"), "fourthTask");
        todoListCommandModel.unlock();

        todoListCommandModelRepository.save(todoListCommandModel);

        Optional<TodoListCommandModel> todoListOptional = todoListCommandModelRepository.find(userId, listId);

        TodoListCommandModel retrievedTodoListCommandModel = todoListOptional.get();
        assertThat(retrievedTodoListCommandModel).isEqualToIgnoringGivenFields(todoListCommandModel, "domainEvents", "version");
    }

    @Test
    public void findsTheMatchingList() {
        UserId userId = new UserId("someIdentifier");
        ListId listId = new ListId("anotherListIdentifier");
        userRepository.save(new User(userId, listId));
        TodoListCommandModel firstTodoListCommandModel = new TodoListCommandModel(clock, userId, new ListId("someListIdentifier"), "someName", Date.from(Instant.parse("2007-12-03T10:15:30.00Z")), new ArrayList<>(), 0);
        TodoListCommandModel secondTodoListCommandModel = new TodoListCommandModel(clock, userId, listId, "someName", Date.from(Instant.parse("2007-12-03T10:15:30.00Z")), new ArrayList<>(), 0);
        TodoListCommandModel thirdTodoListCommandModel = new TodoListCommandModel(clock, userId, new ListId("yetAnotherListIdentifier"), "someName", Date.from(Instant.parse("2007-12-03T10:15:30.00Z")), new ArrayList<>(), 0);
        todoListCommandModelRepository.save(firstTodoListCommandModel);
        todoListCommandModelRepository.save(secondTodoListCommandModel);
        todoListCommandModelRepository.save(thirdTodoListCommandModel);

        Optional<TodoListCommandModel> todoListOptional = todoListCommandModelRepository.find(userId, listId);

        TodoListCommandModel todoListCommandModel = todoListOptional.get();
        assertThat(todoListCommandModel).isEqualToComparingFieldByField(secondTodoListCommandModel);
    }
}