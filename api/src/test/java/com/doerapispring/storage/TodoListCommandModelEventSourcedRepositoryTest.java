package com.doerapispring.storage;

import com.doerapispring.domain.*;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

@SpringBootTest
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@ActiveProfiles(value = "test")
@RunWith(SpringRunner.class)
public class TodoListCommandModelEventSourcedRepositoryTest {
    private TodoListCommandModelEventSourcedRepository todoListCommandModelRepository;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Autowired
    private TodoListDao todoListDao;

    @Autowired
    private TodoListEventStoreRepository todoListEventStoreRepository;

    private Clock clock;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TodoListRepository todoListRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private TodoList todoList;
    private UserId userId;
    private ListId listId;

    @Before
    public void setUp() throws Exception {
        clock = Clock.systemDefaultZone();
        userId = new UserId("someUserIdentifier");
        listId = new ListId("someListIdentifier");
        userRepository.save(new User(userId, listId));
        todoList = new TodoList(userId, listId, "someName");
        todoListRepository.save(todoList);
        todoListCommandModelRepository = new TodoListCommandModelEventSourcedRepository(
                clock,
                todoListDao,
                todoListEventStoreRepository,
                objectMapper);
    }

    @Test
    public void savesTodoList() throws Exception {
        TodoListCommandModel todoListCommandModel = TodoListCommandModel.newInstance(clock, todoList);
        todoListCommandModel.add(new TodoId("someTodoIdentifier"), "someTask");
        TodoId todoIdToMove1 = new TodoId("someDeferredTodoIdentifier1");
        TodoId todoIdToMove2 = new TodoId("someDeferredTodoIdentifier2");
        todoListCommandModel.addDeferred(todoIdToMove1, "someDeferredTask1");
        todoListCommandModel.addDeferred(todoIdToMove2, "someDeferredTask2");
        todoListCommandModel.move(todoIdToMove1, todoIdToMove2);
        TodoId todoIdToDelete = new TodoId("deleteMe");
        todoListCommandModel.addDeferred(todoIdToDelete, "taskToDelete");
        todoListCommandModel.delete(todoIdToDelete);
        TodoId todoIdToUpdate = new TodoId("updateMe");
        todoListCommandModel.addDeferred(todoIdToUpdate, "taskToUpdate");
        todoListCommandModel.update(todoIdToUpdate, "updatedTask");
        todoListCommandModel.pull();
        todoListCommandModel.escalate();
        TodoId todoIdToComplete = new TodoId("completeMe");
        todoListCommandModel.displace(todoIdToComplete, "someImportantTask");
        todoListCommandModel.complete(todoIdToComplete);
        todoListCommandModel.unlock();

        todoListCommandModelRepository.save(todoListCommandModel);

        Optional<TodoListCommandModel> todoListOptional = todoListCommandModelRepository.find(userId, listId);

        TodoListCommandModel retrievedTodoListCommandModel = todoListOptional.get();
        assertThat(retrievedTodoListCommandModel)
                .isEqualToIgnoringGivenFields(todoListCommandModel, "domainEvents", "version");
    }

    @Test
    public void savesAnExistingTodoList() throws Exception {
        TodoListCommandModel todoListCommandModel = TodoListCommandModel.newInstance(clock, todoList);
        todoListCommandModel.add(new TodoId("id1"), "task1");

        todoListCommandModelRepository.save(todoListCommandModel);

        TodoListCommandModel firstRetrievedTodoListCommandModel = todoListCommandModelRepository.find(userId, listId).get();
        firstRetrievedTodoListCommandModel.add(new TodoId("id2"), "task2");

        todoListCommandModelRepository.save(firstRetrievedTodoListCommandModel);

        TodoListCommandModel secondRetrievedTodoListCommandModel = todoListCommandModelRepository.find(userId, listId).get();
        assertThat(secondRetrievedTodoListCommandModel).isEqualToIgnoringGivenFields(firstRetrievedTodoListCommandModel, "domainEvents", "version");
    }

    @Test
    public void retrievedTodoLists_haveNoDomainEventsEmitted() throws Exception {
        TodoListCommandModel todoListCommandModel = TodoListCommandModel.newInstance(clock, todoList);
        todoListCommandModel.add(new TodoId("someTodoIdentifier"), "someTask");

        todoListCommandModelRepository.save(todoListCommandModel);

        Optional<TodoListCommandModel> todoListOptional = todoListCommandModelRepository.find(userId, listId);

        TodoListCommandModel retrievedTodoListCommandModel = todoListOptional.get();
        assertThat(retrievedTodoListCommandModel.getDomainEvents()).isEmpty();
    }

    @Test
    public void retrievesTodoList_withDomainEventsAppliedInVersionOrder() throws Exception {
        TodoListCommandModel todoListCommandModel = TodoListCommandModel.newInstance(clock, todoList);
        todoListCommandModel.add(new TodoId("id1"), "task1");
        todoListCommandModel.add(new TodoId("id2"), "task2");
        TodoListCommandModel todoListCommandModelSpy = spy(todoListCommandModel);
        when(todoListCommandModelSpy.getVersion()).thenReturn(2, 0);

        todoListCommandModelRepository.save(todoListCommandModelSpy);

        TodoListCommandModel expected = TodoListCommandModel.newInstance(clock, todoList);
        expected.add(new TodoId("id2"), "task2");
        expected.add(new TodoId("id1"), "task1");

        Optional<TodoListCommandModel> todoListOptional = todoListCommandModelRepository.find(userId, listId);

        TodoListCommandModel retrievedTodoListCommandModel = todoListOptional.get();
        assertThat(retrievedTodoListCommandModel).isEqualToIgnoringGivenFields(expected, "domainEvents", "version");
    }
}