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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

@SpringBootTest
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@ActiveProfiles(value = "test")
@RunWith(SpringRunner.class)
public class ListOverviewRepositoryTest {

    @Autowired
    private TodoListDao todoListDao;

    @Autowired
    private UserDAO userDao;

    private Clock clock;

    private ListOverviewRepository listOverviewRepository;

    private TodoListRepository todoListRepository;

    private UserRepository userRepository;

    @Before
    public void setUp() throws Exception {
        clock = Clock.systemDefaultZone();
        todoListRepository = new TodoListRepository(userDao, todoListDao, clock, mock(IdGenerator.class));
        userRepository = new UserRepository(userDao);
        listOverviewRepository = new ListOverviewRepository(todoListDao);
    }

    @Test
    public void     readsAllListOverviews() {
        UserId userId = new UserId("someIdentifier");
        userRepository.save(new User(userId));
        UserId otherUserId = new UserId("someOtherIdentifier");
        userRepository.save(new User(otherUserId));
        ListId listId = new ListId("someListIdentifier");
        String listName = "someName";
        TodoList userTodoList = new TodoList(clock, userId, listId, listName, Date.from(Instant.parse("2007-12-03T10:15:30.00Z")), new ArrayList<>(), 0);
        todoListRepository.save(userTodoList);
        ListId otherListId = new ListId("someOtherListIdentifier");
        String otherListName = "someName";
        TodoList otherUserTodoList = new TodoList(clock, otherUserId, otherListId, otherListName, Date.from(Instant.parse("2007-12-03T10:15:30.00Z")), new ArrayList<>(), 0);
        todoListRepository.save(otherUserTodoList);

        List<ListOverview> listOverviews = listOverviewRepository.findAll(userId);
        assertThat(listOverviews).containsOnly(new ListOverview(listId, listName));
    }
}