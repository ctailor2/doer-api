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
    private IdGenerator idGenerator;

    @Before
    public void setUp() throws Exception {
        clock = Clock.systemDefaultZone();
        idGenerator = mock(IdGenerator.class);
        todoListRepository = new TodoListRepository(userDao, todoListDao, idGenerator, clock);
        userRepository = new UserRepository(userDao);
    }

    @Test
    public void savesTodoList() throws Exception {
        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>("someIdentifier");
        userRepository.add(new User(uniqueIdentifier));
        TodoList todoList = new TodoList(clock, new UserId(uniqueIdentifier.get()), Date.from(Instant.parse("2007-12-03T10:15:30.00Z")), new ArrayList<>(), 0);
        todoList.addDeferred(new TodoId("1"), "firstTask");
        todoList.add(new TodoId("2"), "secondTask");
        todoList.addDeferred(new TodoId("3"), "thirdTask");
        todoList.add(new TodoId("4"), "fourthTask");
        todoList.unlock();

        todoListRepository.save(todoList);

        Optional<TodoList> todoListOptional = todoListRepository.findOne(new UserId(uniqueIdentifier.get()));

        TodoList retrievedTodoList = todoListOptional.get();
        assertThat(retrievedTodoList).isEqualToIgnoringGivenFields(todoList, "lastUnlockedAt");
//        Have to 're-wrap' this into a date for the assertion because it comes back from the db as a java.sql.Timestamp
        assertThat(Date.from(retrievedTodoList.getLastUnlockedAt().toInstant())).isCloseTo(todoList.getLastUnlockedAt(), 10L);
    }
}