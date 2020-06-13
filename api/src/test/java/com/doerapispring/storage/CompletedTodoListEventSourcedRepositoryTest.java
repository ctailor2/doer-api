package com.doerapispring.storage;

import com.doerapispring.domain.*;
import com.doerapispring.domain.events.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Date;
import java.time.Instant;
import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static scala.jdk.javaapi.CollectionConverters.asScala;

@SpringBootTest
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@RunWith(SpringRunner.class)
@ActiveProfiles(value = "test")
public class CompletedTodoListEventSourcedRepositoryTest {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TodoListRepository todoListRepository;

    @Autowired
    private CompletedTodoListEventSourcedRepository completedTodoListEventSourcedRepository;

    @Autowired
    private TodoListEventRepository todoListEventRepository;

    private UserId userId;
    private ListId listId;
    private TodoList todoList;

    @Before
    public void setUp() throws Exception {
        userId = new UserId("someUserId");
        listId = new ListId("someListId");
        userRepository.save(new User(userId, listId));
        todoList = new TodoList(userId, listId, "someName");
        todoListRepository.save(todoList);
    }

    @Test
    public void retrievesTheCompletedTodoListMatchingTheUserIdAndListId() {
        ListId otherListId = new ListId("someOtherListId");
        UserId otherUserId = new UserId("someOtherUserId");
        userRepository.save(new User(otherUserId, otherListId));
        TodoList usersOtherList = new TodoList(userId, otherListId, "someName");
        todoListRepository.save(usersOtherList);

        String todoId1 = "someCompletedTodoId";
        todoListEventRepository.saveAll(userId, listId, asScala(Arrays.<TodoListEvent>asList(
                new TodoAddedEvent(todoId1, "someTask"),
                new TodoCompletedEvent(todoId1))).toList());

        String todoId2 = "someOtherCompletedTodoId";
        todoListEventRepository.saveAll(userId, otherListId, asScala(Arrays.<TodoListEvent>asList(
                new TodoAddedEvent(todoId2, "someOtherTask"),
                new TodoCompletedEvent(todoId2))).toList());

        Optional<CompletedTodoList> optionalCompletedTodoList = completedTodoListEventSourcedRepository.find(userId, listId);
        assertThat(optionalCompletedTodoList).isNotEmpty();
        assertThat(optionalCompletedTodoList.get().getTodos())
                .usingElementComparatorIgnoringFields("completedAt")
                .containsExactly(
                        new CompletedTodo(
                                new CompletedTodoId(todoId1),
                                "someTask",
                                Date.from(Instant.EPOCH)));
    }

    @Test
    public void includesCompletedTodosFromAllOrigins() {
        String todoId1 = "todoId1";
        String todoId2 = "todoId2";
        String displacingTodoId = "displacingTodoId";
        String deferredTodoId = "deferredTodoId";
        todoListEventRepository.saveAll(userId, listId, asScala(Arrays.<TodoListEvent>asList(
                new TodoAddedEvent(todoId1, "task1"),
                new TodoAddedEvent(todoId2, "task2"),
                new TodoDisplacedEvent(displacingTodoId, "displacingTask"),
                new DeferredTodoAddedEvent(deferredTodoId, "deferredTask"),
                new TodoCompletedEvent(todoId2),
                new TodoCompletedEvent(displacingTodoId),
                new TodoCompletedEvent(deferredTodoId)
        )).toList());

        Optional<CompletedTodoList> optionalCompletedTodoList = completedTodoListEventSourcedRepository.find(userId, listId);
        assertThat(optionalCompletedTodoList).isNotEmpty();
        assertThat(optionalCompletedTodoList.get().getTodos())
                .usingElementComparatorIgnoringFields("completedAt")
                .contains(
                        new CompletedTodo(
                                new CompletedTodoId(todoId2),
                                "task2",
                                Date.from(Instant.now())),
                        new CompletedTodo(
                                new CompletedTodoId(displacingTodoId),
                                "displacingTask",
                                Date.from(Instant.now())),
                        new CompletedTodo(
                                new CompletedTodoId(deferredTodoId),
                                "deferredTask",
                                Date.from(Instant.now())));
    }

    @Test
    public void retrievesCompletedTodoListWithTodosInDescendingOrderByVersion() {
        String todoId1 = "earlierId";
        String todoId2 = "laterId";
        todoListEventRepository.saveAll(userId, listId, asScala(Arrays.<TodoListEvent>asList(
                new TodoAddedEvent(todoId1, "earlierTask"),
                new TodoCompletedEvent(todoId1),
                new TodoAddedEvent(todoId2, "laterTask"),
                new TodoCompletedEvent(todoId2)
        )).toList());

        Optional<CompletedTodoList> optionalCompletedTodoList = completedTodoListEventSourcedRepository.find(userId, listId);
        assertThat(optionalCompletedTodoList).isNotEmpty();
        assertThat(optionalCompletedTodoList.get().getTodos())
                .usingElementComparatorIgnoringFields("completedAt")
                .containsExactly(
                        new CompletedTodo(
                                new CompletedTodoId(todoId2),
                                "laterTask",
                                Date.from(Instant.now())),
                        new CompletedTodo(
                                new CompletedTodoId(todoId1),
                                "earlierTask",
                                Date.from(Instant.now())));
    }
}