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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(org.mockito.junit.MockitoJUnitRunner.class)
public class ListServiceTest {
    private ListService listService;

    @Mock
    private OwnedObjectRepository<TodoListCommandModel, UserId, ListId> mockTodoListCommandModelRepository;

    @Mock
    private OwnedObjectRepository<CompletedTodoList, UserId, ListId> mockCompletedTodoRepository;

    @Mock
    private OwnedObjectRepository<TodoList, UserId, ListId> mockTodoListRepository;

    @Mock
    private ObjectRepository<User, UserId> mockUserRepository;

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
            mockTodoListFactory,
            mockUserRepository);
        identifier = "userId";
        todoListCommandModel = mock(TodoListCommandModel.class);
        when(mockTodoListCommandModelRepository.find(any(), any())).thenReturn(Optional.of(todoListCommandModel));
    }

    @Test
    public void unlock_whenTodoListFound_unlocksTodoList_andSavesIt() {
        ListId listId = new ListId("someListId");
        listService.unlock(new User(new UserId(identifier), listId), listId);

        verify(todoListCommandModel).unlock();
        verify(mockTodoListCommandModelRepository).save(todoListCommandModel);
    }

    @Test
    public void unlock_whenTodoListNotFound_refusesOperation() {
        when(mockTodoListCommandModelRepository.find(any(), any())).thenReturn(Optional.empty());

        exception.expect(ListNotFoundException.class);
        ListId listId = new ListId("someListId");
        listService.unlock(new User(new UserId(identifier), listId), listId);
    }

    @Test
    public void getDefault_whenTodoListFound_returnsTodoListFromRepository() {
        UserId userId = new UserId(identifier);
        ListId listId = new ListId("someListId");
        TodoListCommandModel mockTodoListCommandModel = mock(TodoListCommandModel.class);
        when(mockTodoListCommandModelRepository.find(userId, listId)).thenReturn(Optional.of(mockTodoListCommandModel));
        TodoListReadModel mockTodoListReadModel = mock(TodoListReadModel.class);
        when(mockTodoListCommandModel.read()).thenReturn(mockTodoListReadModel);
        User user = new User(userId, listId);

        TodoListReadModel actual = listService.getDefault(user);

        assertThat(actual).isEqualTo(mockTodoListReadModel);
    }

    @Test
    public void getDefault_whenTodoListNotFound_refusesOperation() {
        when(mockTodoListCommandModelRepository.find(any(), any())).thenReturn(Optional.empty());

        exception.expect(ListNotFoundException.class);
        listService.getDefault(new User(new UserId(identifier), new ListId("someListId")));
    }

    @Test
    public void get_whenTodoListFound_returnsTodoListFromRepository() {
        TodoListCommandModel mockTodoListCommandModel = mock(TodoListCommandModel.class);
        when(mockTodoListCommandModelRepository.find(any(), any())).thenReturn(Optional.of(mockTodoListCommandModel));
        TodoListReadModel mockTodoListReadModel = mock(TodoListReadModel.class);
        when(mockTodoListCommandModel.read()).thenReturn(mockTodoListReadModel);
        ListId listId = new ListId("someListId");
        User user = new User(new UserId(identifier), listId);

        TodoListReadModel actual = listService.get(user, listId);

        assertThat(actual).isEqualTo(mockTodoListReadModel);
    }

    @Test
    public void get_whenTodoListNotFound_refusesOperation() {
        when(mockTodoListCommandModelRepository.find(any(), any())).thenReturn(Optional.empty());

        exception.expect(ListNotFoundException.class);
        ListId listId = new ListId("someListId");
        listService.get(new User(new UserId(identifier), listId), listId);
    }

    @Test
    public void get_whenCompletedListFound_returnsCompletedListFromRepository() {
        List<CompletedTodo> expectedTodos = singletonList(new CompletedTodo(
            new CompletedTodoId("someTodoId"),
            "someTask",
            Date.from(Instant.now())));
        ListId listId = new ListId("someListId");
        UserId userId = new UserId(identifier);
        when(mockCompletedTodoRepository.find(userId, listId))
            .thenReturn(Optional.of(new CompletedTodoList(userId, listId, expectedTodos)));

        List<CompletedTodo> actualTodos = listService.getCompleted(new User(userId, listId), listId).getTodos();

        assertThat(actualTodos).isEqualTo(expectedTodos);
    }

    @Test
    public void getAll_getsTodoListsFromRepository() {
        UserId userId = new UserId(identifier);
        List<TodoList> expectedTodoLists = singletonList(
            new TodoList(new UserId("someUserId"), new ListId("someListId"), "someName", 0, java.util.Date.from(Instant.EPOCH)));
        when(mockTodoListRepository.findAll(userId))
            .thenReturn(expectedTodoLists);

        List<TodoList> actualTodoLists = listService.getAll(new User(userId, new ListId("anyOldListId")));

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

        listService.create(new User(userId, listId), listName);

        verify(mockTodoListRepository).save(todoList);
    }

    @Test
    public void setDefault_setsTheDefaultListIdForTheUser() {
        UserId userId = new UserId(identifier);
        ListId existingListId = new ListId("someId");
        ListId newListId = new ListId("otherListId");

        listService.setDefault(new User(userId, existingListId), newListId);

        verify(mockUserRepository).save(new User(userId, newListId));
    }
}