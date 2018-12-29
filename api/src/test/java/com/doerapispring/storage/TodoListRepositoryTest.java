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
import org.springframework.util.IdGenerator;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SpringBootTest
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@ActiveProfiles(value = "test")
@RunWith(SpringRunner.class)
public class TodoListRepositoryTest {
    private TodoListRepository todoListRepository;

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
        todoListRepository = new TodoListRepository(userDao, todoListDao, clock, mock(IdGenerator.class));
        userRepository = new UserRepository(userDao);
    }

    @Test
    public void savesTodoList() throws Exception {
        UserId userId = new UserId("someIdentifier");
        userRepository.save(new User(userId));
        ListId listId = new ListId("someListIdentifier");
        TodoList todoList = new TodoList(clock, userId, listId, "someName", Date.from(Instant.parse("2007-12-03T10:15:30.00Z")), new ArrayList<>(), 0);
        todoList.addDeferred(new TodoId("1"), "firstTask");
        todoList.add(new TodoId("2"), "secondTask");
        todoList.addDeferred(new TodoId("3"), "thirdTask");
        todoList.add(new TodoId("4"), "fourthTask");
        todoList.unlock();

        todoListRepository.save(todoList);

        Optional<TodoList> todoListOptional = todoListRepository.findFirst(userId);

        TodoList retrievedTodoList = todoListOptional.get();
        assertThat(retrievedTodoList).isEqualToComparingFieldByField(todoList);
    }

    @Test
    public void findsAllSavedTodoLists() throws Exception {
        UserId userId = new UserId("someIdentifier");
        userRepository.save(new User(userId));
        TodoList firstTodoList = new TodoList(clock, userId, new ListId("firstListIdentifier"), "someName", Date.from(Instant.parse("2007-12-03T10:15:30.00Z")), new ArrayList<>(), 0);
        TodoList secondTodoList = new TodoList(clock, userId, new ListId("secondListIdentifier"), "someName", Date.from(Instant.parse("2007-12-03T10:15:30.00Z")), new ArrayList<>(), 0);
        todoListRepository.save(firstTodoList);
        todoListRepository.save(secondTodoList);

        List<TodoList> todoLists = todoListRepository.findAll(userId);

        assertThat(todoLists).usingFieldByFieldElementComparator()
            .containsExactlyInAnyOrder(firstTodoList, secondTodoList);
    }

    @Test
    public void findsTheMatchingList() throws Exception {
        UserId userId = new UserId("someIdentifier");
        userRepository.save(new User(userId));
        TodoList firstTodoList = new TodoList(clock, userId, new ListId("someListIdentifier"), "someName", Date.from(Instant.parse("2007-12-03T10:15:30.00Z")), new ArrayList<>(), 0);
        ListId listId = new ListId("anotherListIdentifier");
        TodoList secondTodoList = new TodoList(clock, userId, listId, "someName", Date.from(Instant.parse("2007-12-03T10:15:30.00Z")), new ArrayList<>(), 0);
        TodoList thirdTodoList = new TodoList(clock, userId, new ListId("yetAnotherListIdentifier"), "someName", Date.from(Instant.parse("2007-12-03T10:15:30.00Z")), new ArrayList<>(), 0);
        todoListRepository.save(firstTodoList);
        todoListRepository.save(secondTodoList);
        todoListRepository.save(thirdTodoList);

        Optional<TodoList> todoListOptional = todoListRepository.find(userId, listId);

        TodoList todoList = todoListOptional.get();
        assertThat(todoList).isEqualToComparingFieldByField(secondTodoList);
    }
}