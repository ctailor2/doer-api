package com.doerapispring.domain;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Mock;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@RunWith(org.mockito.junit.MockitoJUnitRunner.class)
public class TodoServiceTest {
    private TodoService todoService;

    @Mock
    private OwnedObjectRepository<TodoListCommandModel, UserId, ListId> mockTodoListRepository;

    @Mock
    private IdentityGeneratingRepository<TodoId> mockTodoRepository;

    @Mock
    private DomainEventPublisher domainEventPublisher;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private TodoListCommandModel todoListCommandModel;

    private String todoIdentifier;

    private User user;

    @Before
    public void setUp() throws Exception {
        todoService = new TodoService(
            mockTodoListRepository,
            mockTodoRepository,
            domainEventPublisher
        );
        user = new User(new UserId("userId"));
        todoListCommandModel = mock(TodoListCommandModel.class);
        when(mockTodoListRepository.find(any(), any())).thenReturn(Optional.of(todoListCommandModel));
        todoIdentifier = "todoId";
        when(mockTodoRepository.nextIdentifier()).thenReturn(new TodoId(todoIdentifier));
    }

    @Test
    public void create_whenTodoListFound_addsTodoToRepository() throws Exception {
        String task = "some things";
        todoService.create(user, new ListId("someListId"), task);

        verify(todoListCommandModel).add(new TodoId(todoIdentifier), task);
        verify(mockTodoListRepository).save(todoListCommandModel);
    }

    @Test
    public void create_whenTodoListFound_whenTodoWithTaskExists_refusesCreate() throws Exception {
        doThrow(new DuplicateTodoException()).when(todoListCommandModel).add(any(TodoId.class), any());

        assertThatThrownBy(() ->
            todoService.create(user, new ListId("someListId"), "some things"))
            .isInstanceOf(InvalidCommandException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    public void create_whenTodoListFound_whenListFull_refusesCreate() throws Exception {
        doThrow(new ListSizeExceededException()).when(todoListCommandModel).add(any(TodoId.class), any());

        exception.expect(InvalidCommandException.class);
        todoService.create(user, new ListId("someListId"), "some things");
    }

    @Test
    public void create_whenTodoListNotFound_refusesCreate() throws Exception {
        when(mockTodoListRepository.find(any(), any())).thenReturn(Optional.empty());

        exception.expect(InvalidCommandException.class);
        todoService.create(user, new ListId("someListId"), "some things");
    }

    @Test
    public void createDeferred_whenTodoListFound_addsTodoToRepository() throws Exception {
        String task = "some things";
        todoService.createDeferred(user, new ListId("someListId"), task);

        verify(todoListCommandModel).addDeferred(new TodoId(todoIdentifier), task);
        verify(mockTodoListRepository).save(todoListCommandModel);
    }

    @Test
    public void createDeferred_whenTodoListFound_whenTodoWithTaskExists_refusesCreate() throws Exception {
        doThrow(new DuplicateTodoException()).when(todoListCommandModel).addDeferred(any(TodoId.class), any());

        assertThatThrownBy(() ->
            todoService.createDeferred(user, new ListId("someListId"), "some things"))
            .isInstanceOf(InvalidCommandException.class)
            .hasMessageContaining("already exists");

    }

    @Test
    public void createDeferred_whenTodoListNotFound_refusesCreate() throws Exception {
        when(mockTodoListRepository.find(any(), any())).thenReturn(Optional.empty());

        exception.expect(InvalidCommandException.class);
        todoService.createDeferred(user, new ListId("someListId"), "some things");
    }

    @Test
    public void delete_whenTodoListFound_whenTodoFound_deletesTodoUsingRepository() throws Exception {
        TodoId todoId = new TodoId("someIdentifier");
        todoService.delete(user, new ListId("someListId"), todoId);

        verify(todoListCommandModel).delete(todoId);
        verify(mockTodoListRepository).save(todoListCommandModel);
    }

    @Test
    public void delete_whenTodoListFound_whenTodoNotFound_refusesDelete() throws Exception {
        doThrow(new TodoNotFoundException()).when(todoListCommandModel).delete(any());

        exception.expect(InvalidCommandException.class);
        todoService.delete(user, new ListId("someListId"), new TodoId("someTodoId"));
    }

    @Test
    public void displace_whenTodoListFound_whenTodoFound_savesUsingRepository() throws Exception {
        todoService.displace(user, new ListId("someListId"), "someTask");

        verify(todoListCommandModel).displace(new TodoId(todoIdentifier), "someTask");
        verify(mockTodoListRepository).save(todoListCommandModel);
    }

    @Test
    public void displace_whenTodoListNotFound_refusesDisplace() throws Exception {
        when(mockTodoListRepository.find(any(), any())).thenReturn(Optional.empty());

        exception.expect(InvalidCommandException.class);
        todoService.displace(user, new ListId("someListId"), "someTask");
    }

    @Test
    public void displace_whenTodoListFound_whenTodoFound_whenTodoWithTaskExists_refusesDisplace() throws Exception {
        doThrow(new DuplicateTodoException()).when(todoListCommandModel).displace(any(TodoId.class), any());

        assertThatThrownBy(() ->
            todoService.displace(user, new ListId("someListId"), "someTask"))
            .isInstanceOf(InvalidCommandException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    public void displace_whenTodoListFound_whenTodoFound_whenListIsNotFull_refusesDisplace() throws Exception {
        doThrow(new ListNotFullException()).when(todoListCommandModel).displace(any(TodoId.class), any());

        exception.expect(InvalidCommandException.class);
        todoService.displace(user, new ListId("someListId"), "someTask");
    }

    @Test
    public void update_whenTodoListFound_whenTodoFound_updatesUsingRepository() throws Exception {
        String updatedTask = "someOtherTask";
        TodoId todoId = new TodoId("someIdentifier");
        todoService.update(user, new ListId("someListId"), todoId, updatedTask);

        verify(todoListCommandModel).update(todoId, updatedTask);
        verify(mockTodoListRepository).save(todoListCommandModel);
    }

    @Test
    public void update_whenTodoListFound_whenTodoFound_whenTodoWithTaskExists_refusesUpdate() throws Exception {
        doThrow(new DuplicateTodoException()).when(todoListCommandModel).update(any(), any());

        assertThatThrownBy(() ->
            todoService.update(user, new ListId("someListId"), new TodoId("someIdentifier"), "someTask"))
            .isInstanceOf(InvalidCommandException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    public void update_whenTodoListFound_whenTodoNotFound_refusesUpdate() throws Exception {
        doThrow(new TodoNotFoundException()).when(todoListCommandModel).update(any(), any());

        exception.expect(InvalidCommandException.class);
        todoService.update(user, new ListId("someListId"), new TodoId("someId"), "someTask");
    }

    @Test
    public void update_whenTodoListNotFound_refusesUpdate() throws Exception {
        when(mockTodoListRepository.find(any(), any())).thenReturn(Optional.empty());

        exception.expect(InvalidCommandException.class);
        todoService.update(user, new ListId("someListId"), new TodoId("someId"), "someTask");
    }

    @Test
    public void complete_whenTodoListFound_whenTodoFound_completesTodo_savesUsingRepository() throws Exception {
        TodoId todoId = new TodoId("someIdentifier");
        todoService.complete(user, new ListId("someListId"), todoId);

        verify(todoListCommandModel).complete(todoId);
        verify(mockTodoListRepository).save(todoListCommandModel);
    }

    @Test
    public void complete_publishesDomainEvents_afterCompleting() throws Exception {
        todoService.complete(user, new ListId("someListId"), new TodoId("someIdentifier"));

        InOrder inOrder = inOrder(domainEventPublisher, todoListCommandModel);
        inOrder.verify(todoListCommandModel).complete(any());
        inOrder.verify(domainEventPublisher).publish(todoListCommandModel);
    }

    @Test
    public void complete_whenTodoListFound_whenTodoNotFound_refusesUpdate() throws Exception {
        doThrow(new TodoNotFoundException()).when(todoListCommandModel).complete(any());

        exception.expect(InvalidCommandException.class);
        todoService.complete(user, new ListId("someListId"), new TodoId("someId"));
    }

    @Test
    public void complete_whenTodoListNotFound_refusesUpdate() throws Exception {
        when(mockTodoListRepository.find(any(), any())).thenReturn(Optional.empty());

        exception.expect(InvalidCommandException.class);
        todoService.complete(user, new ListId("someListId"), new TodoId("someId"));
    }

    @Test
    public void move_whenTodoListFound_whenTodosFound_updatesMovedTodosUsingRepository() throws Exception {
        TodoId sourceIdentifier = new TodoId("sourceIdentifier");
        TodoId destinationIdentifier = new TodoId("destinationIdentifier");
        todoService.move(user, new ListId("someListId"), sourceIdentifier, destinationIdentifier);

        verify(todoListCommandModel).move(sourceIdentifier, destinationIdentifier);
        verify(mockTodoListRepository).save(todoListCommandModel);
    }

    @Test
    public void move_whenTodoListFound_whenTodosNotFound_refusesOperation() throws Exception {
        doThrow(new TodoNotFoundException()).when(todoListCommandModel).move(any(), any());

        exception.expect(InvalidCommandException.class);
        todoService.move(user, new ListId("someListId"), new TodoId("idOne"), new TodoId("idTwo"));
    }

    @Test
    public void move_whenTodoListNotFound_refusesOperation() throws Exception {
        when(mockTodoListRepository.find(any(), any())).thenReturn(Optional.empty());

        exception.expect(InvalidCommandException.class);
        todoService.move(user, new ListId("someListId"), new TodoId("idOne"), new TodoId("idTwo"));
    }

    @Test
    public void pull_whenTodoListFound_whenTodosPulled_updatesPulledTodosUsingRepository() throws Exception {
        todoService.pull(user, new ListId("someListId"));

        verify(todoListCommandModel).pull();
        verify(mockTodoListRepository).save(todoListCommandModel);
    }

    @Test
    public void escalate_escalatesList_andSavesIt() throws Exception {
        todoService.escalate(user, new ListId("someListId"));

        verify(todoListCommandModel).escalate();
        verify(mockTodoListRepository).save(todoListCommandModel);
    }

    @Test
    public void escalate_whenEscalateNotAllowed_refusesOperation() throws Exception {
        doThrow(new EscalateNotAllowException()).when(todoListCommandModel).escalate();

        exception.expect(InvalidCommandException.class);
        todoService.escalate(user, new ListId("someListId"));
    }

    @Test
    public void escalate_whenListNotFound_refusesOperation() throws Exception {
        when(mockTodoListRepository.find(any(), any())).thenReturn(Optional.empty());

        exception.expect(InvalidCommandException.class);
        todoService.escalate(user, new ListId("someListId"));
    }
}