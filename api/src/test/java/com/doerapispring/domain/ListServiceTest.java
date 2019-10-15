package com.doerapispring.domain;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;

import java.sql.Date;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(org.mockito.junit.MockitoJUnitRunner.class)
public class ListServiceTest {
    private ListService listService;

    @Mock
    private OwnedObjectRepository<TodoListCommandModel, UserId, ListId> mockTodoListCommandModelRepository;

    @Mock
    private OwnedObjectRepository<CompletedTodo, UserId, CompletedTodoId> mockCompletedTodoRepository;

    @Mock
    private OwnedObjectRepository<TodoList, UserId, ListId> mockTodoListRepository;

    @Rule
    public ExpectedException exception = ExpectedException.none();
    private TodoListCommandModel todoListCommandModel;
    private String identifier;

    private TodoListFactory mockTodoListFactory = mock(TodoListFactory.class);

    @Before
    public void setUp() throws Exception {
        listService = new ListService(
            mockTodoListCommandModelRepository,
            mockCompletedTodoRepository,
            mockTodoListRepository,
            mockTodoListFactory);
        identifier = "userId";
        todoListCommandModel = mock(TodoListCommandModel.class);
        when(mockTodoListCommandModelRepository.findFirst(any())).thenReturn(Optional.of(todoListCommandModel));
        when(mockTodoListCommandModelRepository.find(any(), any())).thenReturn(Optional.of(todoListCommandModel));
    }

    @Test
    public void unlock_whenTodoListFound_unlocksTodoList_andSavesIt() throws Exception {
        listService.unlock(new User(new UserId(identifier)), new ListId("someListId"));

        verify(todoListCommandModel).unlock();
        verify(mockTodoListCommandModelRepository).save(todoListCommandModel);
    }

    @Test
    public void unlock_whenTodoListFound_whenLockTimerNotExpired_refusesOperation() throws Exception {
        doThrow(new LockTimerNotExpiredException()).when(todoListCommandModel).unlock();

        exception.expect(InvalidCommandException.class);
        listService.unlock(new User(new UserId(identifier)), new ListId("someListId"));
    }

    @Test
    public void unlock_whenTodoListNotFound_refusesOperation() throws Exception {
        when(mockTodoListCommandModelRepository.find(any(), any())).thenReturn(Optional.empty());

        exception.expect(InvalidCommandException.class);
        listService.unlock(new User(new UserId(identifier)), new ListId("someListId"));
    }

    @Test
    public void getDefault_whenTodoListFound_returnsTodoListFromRepository() throws Exception {
        TodoListCommandModel mockTodoListCommandModel = mock(TodoListCommandModel.class);
        when(mockTodoListCommandModelRepository.findFirst(any())).thenReturn(Optional.of(mockTodoListCommandModel));
        TodoListReadModel mockTodoListReadModel = mock(TodoListReadModel.class);
        when(mockTodoListCommandModel.read()).thenReturn(mockTodoListReadModel);
        User user = new User(new UserId(identifier));

        TodoListReadModel actual = listService.getDefault(user);

        assertThat(actual).isEqualTo(mockTodoListReadModel);
    }

    @Test
    public void getDefault_whenTodoListNotFound_refusesOperation() throws Exception {
        when(mockTodoListCommandModelRepository.findFirst(any())).thenReturn(Optional.empty());

        exception.expect(InvalidCommandException.class);
        listService.getDefault(new User(new UserId(identifier)));
    }

    @Test
    public void get_whenTodoListFound_returnsTodoListFromRepository() throws Exception {
        TodoListCommandModel mockTodoListCommandModel = mock(TodoListCommandModel.class);
        when(mockTodoListCommandModelRepository.find(any(), any())).thenReturn(Optional.of(mockTodoListCommandModel));
        TodoListReadModel mockTodoListReadModel = mock(TodoListReadModel.class);
        when(mockTodoListCommandModel.read()).thenReturn(mockTodoListReadModel);
        User user = new User(new UserId(identifier));

        TodoListReadModel actual = listService.get(user, new ListId("someListId"));

        assertThat(actual).isEqualTo(mockTodoListReadModel);
    }

    @Test
    public void get_whenTodoListNotFound_refusesOperation() throws Exception {
        when(mockTodoListCommandModelRepository.find(any(), any())).thenReturn(Optional.empty());

        exception.expect(InvalidCommandException.class);
        listService.get(new User(new UserId(identifier)), new ListId("someListId"));
    }

    @Test
    public void get_whenCompletedListFound_returnsCompletedListFromRepository() throws Exception {
        ListId listId = new ListId("someListId");
        List<CompletedTodo> expectedTodos = singletonList(new CompletedTodo(
            new UserId("someUserId"),
            listId,
            new CompletedTodoId("someTodoId"),
            "someTask",
            Date.from(Instant.now())));
        when(mockCompletedTodoRepository.findAll(any(UserId.class))).thenReturn(expectedTodos);

        List<CompletedTodo> actualTodos = listService.getCompleted(new User(new UserId(identifier)), listId);

        assertThat(actualTodos).isEqualTo(expectedTodos);
    }

    @Test
    public void getAll_getsTodoListsFromRepository() {
        UserId userId = new UserId(identifier);
        List<TodoList> expectedTodoLists = singletonList(
            new TodoList(new UserId("someUserId"), new ListId("someListId"), "someName", 0, java.util.Date.from(Instant.EPOCH)));
        when(mockTodoListRepository.findAll(userId))
            .thenReturn(expectedTodoLists);

        List<TodoList> actualTodoLists = listService.getAll(new User(userId));

        assertThat(actualTodoLists).isEqualTo(expectedTodoLists);
    }

    @Test
    public void create_createsTodoList() {
        UserId userId = new UserId(identifier);
        ListId listId = new ListId("someId");
        String listName = "someName";
        when(mockTodoListRepository.nextIdentifier()).thenReturn(listId);
        TodoList todoList = mock(TodoList.class);
        when(mockTodoListFactory.todoList(userId, listId, listName)).thenReturn(todoList);

        listService.create(new User(userId), listName);

        verify(mockTodoListRepository).save(todoList);
    }
}