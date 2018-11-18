package com.doerapispring.storage;

import com.doerapispring.domain.TodoId;
import com.doerapispring.domain.TodoList;
import com.doerapispring.domain.User;
import com.doerapispring.domain.UserId;
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
        todoListRepository = new TodoListRepository(userDao, todoListDao, clock);
        userRepository = new UserRepository(userDao);
    }

    @Test
    public void savesTodoList() throws Exception {
        UserId userId = new UserId("someIdentifier");
        userRepository.save(new User(userId));
        TodoList todoList = new TodoList(clock, userId, Date.from(Instant.parse("2007-12-03T10:15:30.00Z")), new ArrayList<>(), 0);
        todoList.addDeferred(new TodoId("1"), "firstTask");
        todoList.add(new TodoId("2"), "secondTask");
        todoList.addDeferred(new TodoId("3"), "thirdTask");
        todoList.add(new TodoId("4"), "fourthTask");
        todoList.unlock();

        todoListRepository.save(todoList);

        Optional<TodoList> todoListOptional = todoListRepository.findOne(userId);

        TodoList retrievedTodoList = todoListOptional.get();
        assertThat(retrievedTodoList).isEqualToComparingFieldByField(todoList);
    }
}