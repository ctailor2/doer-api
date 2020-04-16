package com.doerapispring.domain;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;

import java.util.Optional;

import static org.mockito.Mockito.*;

@RunWith(org.mockito.junit.MockitoJUnitRunner.class)
public class TodoServiceTest {
    private TodoService todoService;

    @Mock
    private OwnedObjectRepository<TodoListCommandModel, UserId, ListId> mockTodoListRepository;

    @Mock
    private IdentityGeneratingRepository<TodoId> mockTodoRepository;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private TodoListCommandModel todoListCommandModel;

    private String todoIdentifier;

    private User user;

    @Before
    public void setUp() throws Exception {
        todoService = new TodoService(
            mockTodoListRepository,
            mockTodoRepository
        );
        user = new User(new UserId("userId"), new ListId("someListId"));
        todoListCommandModel = mock(TodoListCommandModel.class);
        when(mockTodoListRepository.find(any(), any())).thenReturn(Optional.of(todoListCommandModel));
        todoIdentifier = "todoId";
        when(mockTodoRepository.nextIdentifier()).thenReturn(new TodoId(todoIdentifier));
    }

    @Test
    public void create_whenTodoListFound_addsTodoToRepository() {
        String task = "some things";
        todoService.create(user, new ListId("someListId"), task);

        verify(todoListCommandModel).add(new TodoId(todoIdentifier), task);
        verify(mockTodoListRepository).save(todoListCommandModel);
    }

    @Test
    public void create_whenTodoListNotFound_refusesCreate() {
        when(mockTodoListRepository.find(any(), any())).thenReturn(Optional.empty());

        exception.expect(ListNotFoundException.class);
        todoService.create(user, new ListId("someListId"), "some things");
    }

    @Test
    public void createDeferred_whenTodoListFound_addsTodoToRepository() {
        String task = "some things";
        todoService.createDeferred(user, new ListId("someListId"), task);

        verify(todoListCommandModel).addDeferred(new TodoId(todoIdentifier), task);
        verify(mockTodoListRepository).save(todoListCommandModel);
    }

    @Test
    public void createDeferred_whenTodoListNotFound_refusesCreate() {
        when(mockTodoListRepository.find(any(), any())).thenReturn(Optional.empty());

        exception.expect(ListNotFoundException.class);
        todoService.createDeferred(user, new ListId("someListId"), "some things");
    }

    @Test
    public void delete_whenTodoListFound_whenTodoFound_deletesTodoUsingRepository() {
        TodoId todoId = new TodoId("someIdentifier");
        todoService.delete(user, new ListId("someListId"), todoId);

        verify(todoListCommandModel).delete(todoId);
        verify(mockTodoListRepository).save(todoListCommandModel);
    }

    @Test
    public void displace_whenTodoListFound_whenTodoFound_savesUsingRepository() {
        todoService.displace(user, new ListId("someListId"), "someTask");

        verify(todoListCommandModel).displace(new TodoId(todoIdentifier), "someTask");
        verify(mockTodoListRepository).save(todoListCommandModel);
    }

    @Test
    public void displace_whenTodoListNotFound_refusesDisplace() {
        when(mockTodoListRepository.find(any(), any())).thenReturn(Optional.empty());

        exception.expect(ListNotFoundException.class);
        todoService.displace(user, new ListId("someListId"), "someTask");
    }

    @Test
    public void update_whenTodoListFound_whenTodoFound_updatesUsingRepository() {
        String updatedTask = "someOtherTask";
        TodoId todoId = new TodoId("someIdentifier");
        todoService.update(user, new ListId("someListId"), todoId, updatedTask);

        verify(todoListCommandModel).update(todoId, updatedTask);
        verify(mockTodoListRepository).save(todoListCommandModel);
    }

    @Test
    public void update_whenTodoListNotFound_refusesUpdate() {
        when(mockTodoListRepository.find(any(), any())).thenReturn(Optional.empty());

        exception.expect(ListNotFoundException.class);
        todoService.update(user, new ListId("someListId"), new TodoId("someId"), "someTask");
    }

    @Test
    public void complete_whenTodoListFound_whenTodoFound_completesTodo_savesUsingRepository() {
        TodoId todoId = new TodoId("someIdentifier");
        todoService.complete(user, new ListId("someListId"), todoId);

        InOrder inOrder = inOrder(mockTodoListRepository, todoListCommandModel);
        inOrder.verify(todoListCommandModel).complete(todoId);
        inOrder.verify(mockTodoListRepository).save(todoListCommandModel);
    }

    @Test
    public void complete_whenTodoListNotFound_refusesUpdate() {
        when(mockTodoListRepository.find(any(), any())).thenReturn(Optional.empty());

        exception.expect(ListNotFoundException.class);
        todoService.complete(user, new ListId("someListId"), new TodoId("someId"));
    }

    @Test
    public void move_whenTodoListFound_whenTodosFound_updatesMovedTodosUsingRepository() {
        TodoId sourceIdentifier = new TodoId("sourceIdentifier");
        TodoId destinationIdentifier = new TodoId("destinationIdentifier");
        todoService.move(user, new ListId("someListId"), sourceIdentifier, destinationIdentifier);

        verify(todoListCommandModel).move(sourceIdentifier, destinationIdentifier);
        verify(mockTodoListRepository).save(todoListCommandModel);
    }

    @Test
    public void move_whenTodoListNotFound_refusesOperation() {
        when(mockTodoListRepository.find(any(), any())).thenReturn(Optional.empty());

        exception.expect(ListNotFoundException.class);
        todoService.move(user, new ListId("someListId"), new TodoId("idOne"), new TodoId("idTwo"));
    }

    @Test
    public void pull_whenTodoListFound_whenTodosPulled_updatesPulledTodosUsingRepository() {
        todoService.pull(user, new ListId("someListId"));

        verify(todoListCommandModel).pull();
        verify(mockTodoListRepository).save(todoListCommandModel);
    }

    @Test
    public void escalate_escalatesList_andSavesIt() {
        todoService.escalate(user, new ListId("someListId"));

        verify(todoListCommandModel).escalate();
        verify(mockTodoListRepository).save(todoListCommandModel);
    }

    @Test
    public void escalate_whenListNotFound_refusesOperation() {
        when(mockTodoListRepository.find(any(), any())).thenReturn(Optional.empty());

        exception.expect(ListNotFoundException.class);
        todoService.escalate(user, new ListId("someListId"));
    }
}