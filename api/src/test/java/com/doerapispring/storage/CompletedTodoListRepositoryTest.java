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
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@RunWith(SpringRunner.class)
@ActiveProfiles(value = "test")
public class CompletedTodoListRepositoryTest {
    @Autowired
    private CompletedTodoRepository completedTodoRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TodoListRepository todoListRepository;

    @Autowired
    private CompletedTodoListRepository completedTodoListRepository;

    private UserId userId;
    private ListId listId;

    @Before
    public void setUp() throws Exception {
        userId = new UserId("someUserId");
        listId = new ListId("someListId");
        userRepository.save(new User(userId, listId));
        todoListRepository.save(new TodoList(userId, listId, "someName", 0, Date.from(Instant.EPOCH)));
    }

    @Test
    public void retrievesTheCompletedTodoListMatchingTheUserIdAndListId() {
        ListId otherListId = new ListId("someOtherListId");
        UserId otherUserId = new UserId("someOtherUserId");
        userRepository.save(new User(otherUserId, otherListId));
        todoListRepository.save(new TodoList(otherUserId, otherListId, "someName", 0, Date.from(Instant.EPOCH)));
        CompletedTodoWriteModel matchingCompletedTodo = new CompletedTodoWriteModel(
            userId,
            listId,
            new CompletedTodoId("someCompletedTodoId"),
            "someTask",
            Date.from(Instant.EPOCH));
        completedTodoRepository.save(matchingCompletedTodo);
        completedTodoRepository.save(new CompletedTodoWriteModel(
            userId,
            otherListId,
            new CompletedTodoId("someOtherCompletedTodoId"),
            "someTask",
            Date.from(Instant.EPOCH)));
        completedTodoRepository.save(new CompletedTodoWriteModel(
            otherUserId,
            listId,
            new CompletedTodoId("yetAnotherCompletedTodoId"),
            "someTask",
            Date.from(Instant.EPOCH)));

        Optional<CompletedTodoList> optionalCompletedTodoList = completedTodoListRepository.find(userId, listId);
        assertThat(optionalCompletedTodoList).isNotEmpty();
        assertThat(optionalCompletedTodoList.get().getTodos()).containsExactly(
            new CompletedTodoReadModel(
                matchingCompletedTodo.getCompletedTodoId(),
                matchingCompletedTodo.getTask(),
                matchingCompletedTodo.getCompletedAt()));
    }

    @Test
    public void retrievesCompletedTodoListWithTodosInDescendingOrderByCompletedAt() {
        Instant now = Instant.now();
        CompletedTodoWriteModel earlierCompletedTodo = new CompletedTodoWriteModel(
            userId,
            listId,
            new CompletedTodoId("earlierId"),
            "earlierTask",
            Date.from(now.minusSeconds(20)));
        CompletedTodoWriteModel laterCompletedTodo = new CompletedTodoWriteModel(
            userId,
            listId,
            new CompletedTodoId("laterId"),
            "laterTask",
            Date.from(now.plusSeconds(20)));
        completedTodoRepository.save(earlierCompletedTodo);
        completedTodoRepository.save(laterCompletedTodo);

        Optional<CompletedTodoList> optionalCompletedTodoList = completedTodoListRepository.find(userId, listId);
        assertThat(optionalCompletedTodoList).isNotEmpty();
        assertThat(optionalCompletedTodoList.get().getTodos()).containsExactly(
            new CompletedTodoReadModel(
                laterCompletedTodo.getCompletedTodoId(),
                laterCompletedTodo.getTask(),
                laterCompletedTodo.getCompletedAt()),
            new CompletedTodoReadModel(
                earlierCompletedTodo.getCompletedTodoId(),
                earlierCompletedTodo.getTask(),
                earlierCompletedTodo.getCompletedAt()));
    }
}