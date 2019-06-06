package com.doerapispring.storage;

import com.doerapispring.domain.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.util.IdGenerator;

import java.time.Clock;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SpringBootTest
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@RunWith(SpringRunner.class)
@ActiveProfiles(value = "test")
public class CompletedTodoRepositoryTest {
    @Autowired
    private CompletedTodoDAO completedTodoDAO;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private Clock clock;

    @Autowired
    private TodoListDao todoListDao;

    @Autowired
    private IdGenerator idGenerator;

    @Autowired
    private TodoListFactory todoListFactory;

    private UserRepository userRepository;

    private CompletedTodoRepository completedTodoRepository;

    private TodoListRepository todoListRepository;

    private CompletedTodoListRepository completedTodoListRepository;

    @Before
    public void setUp() throws Exception {
        completedTodoRepository = new CompletedTodoRepository(completedTodoDAO, userDAO, mock(IdGenerator.class));
        userRepository = new UserRepository(userDAO);
        todoListRepository = new TodoListRepository(userDAO, todoListDao, clock, idGenerator);
        completedTodoListRepository = new CompletedTodoListRepository(completedTodoDAO);
    }

    @Test
    public void savesCompletedTodos() {
        UserId userId = new UserId("someUserId");
        userRepository.save(new User(userId));
        ListId listId = new ListId("someListId");
        todoListRepository.save(todoListFactory.todoList(userId, listId, "someListName"));

        UserId differentUserId = new UserId("differentUserId");
        userRepository.save(new User(differentUserId));
        completedTodoRepository.save(
            new CompletedTodo(
                differentUserId,
                listId,
                new CompletedTodoId("2"),
                "someTask",
                new Date()));
        CompletedTodo completedTodo = new CompletedTodo(
            userId,
            listId,
            new CompletedTodoId("someCompletedTodoId"),
            "someTask",
            new Date());
        completedTodoRepository.save(completedTodo);
        ListId differentListId = new ListId("differentListId");
        todoListRepository.save(todoListFactory.todoList(userId, differentListId, "someListName"));
        completedTodoRepository.save(
            new CompletedTodo(
                userId,
                differentListId,
                new CompletedTodoId("3"),
                "someTask",
                new Date()));

        Optional<CompletedTodoList> completedTodoList = completedTodoListRepository.find(userId, listId);
        assertThat(completedTodoList).isPresent();
        assertThat(completedTodoList.get().getListId()).isEqualTo(listId);
        assertThat(completedTodoList.get().getCompletedTodos()).containsExactly(completedTodo);
    }
}