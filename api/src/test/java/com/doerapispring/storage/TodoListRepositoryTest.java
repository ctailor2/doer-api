package com.doerapispring.storage;

import com.doerapispring.domain.ListId;
import com.doerapispring.domain.TodoList;
import com.doerapispring.domain.User;
import com.doerapispring.domain.UserId;
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
import java.time.Instant;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SpringBootTest
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@ActiveProfiles(value = "test")
@RunWith(SpringRunner.class)
public class TodoListRepositoryTest {

    @Autowired
    private TodoListDao todoListDao;

    @Autowired
    private UserDAO userDao;

    private Clock clock;

    private TodoListRepository todoListRepository;

    private UserRepository userRepository;

    @Before
    public void setUp() throws Exception {
        clock = Clock.systemDefaultZone();
        userRepository = new UserRepository(userDao);
        todoListRepository = new TodoListRepository(userDao, todoListDao, mock(IdGenerator.class));
    }

    @Test
    public void readsAllTodoLists() {
        UserId userId = new UserId("someIdentifier");
        userRepository.save(new User(userId));
        UserId otherUserId = new UserId("someOtherIdentifier");
        userRepository.save(new User(otherUserId));
        ListId listId = new ListId("someListIdentifier");
        String listName = "someName";
        TodoList userTodoList = new TodoList(userId, listId, listName, 0, Date.from(Instant.EPOCH));
        todoListRepository.save(userTodoList);
        ListId otherListId = new ListId("someOtherListIdentifier");
        String otherListName = "someName";
        TodoList otherUserTodoList = new TodoList(otherUserId, otherListId, otherListName, 0, Date.from(Instant.EPOCH));
        todoListRepository.save(otherUserTodoList);

        List<TodoList> todoLists = todoListRepository.findAll(userId);
        assertThat(todoLists).containsOnly(new TodoList(userId, listId, listName, 0, Date.from(Instant.EPOCH)));
    }

    @Test
    public void savesTodoList() {
        UserId userId = new UserId("someUserId");
        userRepository.save(new User(userId));
        TodoList savedTodoList = new TodoList(userId, new ListId("someListId"), "someName", 0, Date.from(Instant.EPOCH));

        todoListRepository.save(savedTodoList);

        List<TodoList> todoLists = todoListRepository.findAll(userId);
        assertThat(todoLists).contains(savedTodoList);
    }
}