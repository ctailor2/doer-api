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

import java.sql.Date;
import java.time.Clock;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

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
    private TodoListCommandModelEventSourcedRepository todoListCommandModelEventSourcedRepository;

    private Clock clock = mock(Clock.class);

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
        when(clock.instant()).thenReturn(Instant.EPOCH);
    }

    @Test
    public void retrievesTheCompletedTodoListMatchingTheUserIdAndListId() {
        ListId otherListId = new ListId("someOtherListId");
        UserId otherUserId = new UserId("someOtherUserId");
        userRepository.save(new User(otherUserId, otherListId));
        TodoList usersOtherList = new TodoList(userId, otherListId, "someName");
        todoListRepository.save(usersOtherList);

        TodoListCommandModel todoListCommandModel = TodoListCommandModel.newInstance(clock, todoList);
        TodoId todoId1 = new TodoId("someCompletedTodoId");
        todoListCommandModel.add(todoId1, "someTask");
        todoListCommandModel.complete(todoId1);
        todoListCommandModelEventSourcedRepository.save(todoListCommandModel);

        TodoListCommandModel usersOtherTodoListCommandModel = TodoListCommandModel.newInstance(clock, usersOtherList);
        TodoId todoId2 = new TodoId("someOtherCompletedTodoId");
        usersOtherTodoListCommandModel.add(todoId2, "someOtherTask");
        usersOtherTodoListCommandModel.complete(todoId2);
        todoListCommandModelEventSourcedRepository.save(usersOtherTodoListCommandModel);

        Optional<CompletedTodoList> optionalCompletedTodoList = completedTodoListEventSourcedRepository.find(userId, listId);
        assertThat(optionalCompletedTodoList).isNotEmpty();
        assertThat(optionalCompletedTodoList.get().getTodos())
                .usingElementComparatorIgnoringFields("completedAt")
                .containsExactly(
                        new CompletedTodo(
                                new CompletedTodoId(todoId1.getIdentifier()),
                                "someTask",
                                Date.from(Instant.EPOCH)));
    }

    @Test
    public void includesCompletedTodosFromAllOrigins() {
        TodoListCommandModel todoListCommandModel = TodoListCommandModel.newInstance(clock, todoList);
        todoListCommandModel.add(new TodoId("todoId1"), "task1");
        TodoId todoId2 = new TodoId("todoId2");
        todoListCommandModel.add(todoId2, "task2");
        TodoId displacingTodoId = new TodoId("displacingTodoId");
        todoListCommandModel.displace(displacingTodoId, "displacingTask");
        TodoId deferredTodoId = new TodoId("deferredTodoId");
        todoListCommandModel.addDeferred(deferredTodoId, "deferredTask");
        todoListCommandModel.complete(todoId2);
        todoListCommandModel.complete(displacingTodoId);
        todoListCommandModel.complete(deferredTodoId);
        todoListCommandModelEventSourcedRepository.save(todoListCommandModel);

        Optional<CompletedTodoList> optionalCompletedTodoList = completedTodoListEventSourcedRepository.find(userId, listId);
        assertThat(optionalCompletedTodoList).isNotEmpty();
        assertThat(optionalCompletedTodoList.get().getTodos())
                .usingElementComparatorIgnoringFields("completedAt")
                .contains(
                        new CompletedTodo(
                                new CompletedTodoId(todoId2.getIdentifier()),
                                "task2",
                                Date.from(Instant.now())),
                        new CompletedTodo(
                                new CompletedTodoId(displacingTodoId.getIdentifier()),
                                "displacingTask",
                                Date.from(Instant.now())),
                        new CompletedTodo(
                                new CompletedTodoId(deferredTodoId.getIdentifier()),
                                "deferredTask",
                                Date.from(Instant.now())));
    }

    @Test
    public void retrievesCompletedTodoListWithTodosInDescendingOrderByVersion() {
        Instant now = Instant.now();
        Instant earlierInstant = now.minusSeconds(20);
        Instant laterInstant = now.plusSeconds(20);
        when(clock.instant()).thenReturn(earlierInstant, laterInstant);
        TodoListCommandModel todoListCommandModel = TodoListCommandModel.newInstance(clock, todoList);
        TodoId todoId1 = new TodoId("earlierId");
        todoListCommandModel.add(todoId1, "earlierTask");
        todoListCommandModel.complete(todoId1);
        todoListCommandModelEventSourcedRepository.save(todoListCommandModel);
        TodoListCommandModel updatedTodoListCommandModel = todoListCommandModelEventSourcedRepository.find(userId, listId).get();
        TodoId todoId2 = new TodoId("laterId");
        updatedTodoListCommandModel.add(todoId2, "laterTask");
        updatedTodoListCommandModel.complete(todoId2);
        todoListCommandModelEventSourcedRepository.save(updatedTodoListCommandModel);

        Optional<CompletedTodoList> optionalCompletedTodoList = completedTodoListEventSourcedRepository.find(userId, listId);
        assertThat(optionalCompletedTodoList).isNotEmpty();
        assertThat(optionalCompletedTodoList.get().getTodos())
                .usingElementComparatorIgnoringFields("completedAt")
                .containsExactly(
                        new CompletedTodo(
                                new CompletedTodoId(todoId2.getIdentifier()),
                                "laterTask",
                                Date.from(laterInstant)),
                        new CompletedTodo(
                                new CompletedTodoId(todoId1.getIdentifier()),
                                "earlierTask",
                                Date.from(earlierInstant)));
    }
}