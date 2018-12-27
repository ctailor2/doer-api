package com.doerapispring.domain;

import com.doerapispring.web.InvalidRequestException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.sql.Date;
import java.time.Instant;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TodoServiceTest {
    private TodoService todoService;

    @Mock
    private OwnedObjectRepository<CompletedTodo, UserId, CompletedTodoId> mockCompletedTodoRepository;

    @Mock
    private OwnedObjectRepository<TodoList, UserId, ListId> mockTodoListRepository;

    @Mock
    private IdentityGeneratingRepository<TodoId> mockTodoRepository;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    private TodoList todoList;

    private String todoIdentifier;

    private User user;

    @Before
    public void setUp() throws Exception {
        todoService = new TodoService(
            mockTodoListRepository,
            mockTodoRepository,
            mockCompletedTodoRepository
        );
        user = new User(new UserId("userId"));
        todoList = mock(TodoList.class);
        when(mockTodoListRepository.findOne(any())).thenReturn(Optional.of(todoList));
        todoIdentifier = "todoId";
        when(mockTodoRepository.nextIdentifier()).thenReturn(new TodoId(todoIdentifier));
    }

    @Test
    public void create_whenTodoListFound_addsTodoToRepository() throws Exception {
        String task = "some things";
        todoService.create(user, new ListId("someListId"), task);

        verify(todoList).add(new TodoId(todoIdentifier), task);
        verify(mockTodoListRepository).save(todoList);
    }

    @Test
    public void create_whenTodoListFound_whenTodoWithTaskExists_refusesCreate() throws Exception {
        doThrow(new DuplicateTodoException()).when(todoList).add(any(TodoId.class), any());

        assertThatThrownBy(() ->
            todoService.create(user, new ListId("someListId"), "some things"))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    public void create_whenTodoListFound_whenListFull_refusesCreate() throws Exception {
        doThrow(new ListSizeExceededException()).when(todoList).add(any(TodoId.class), any());

        exception.expect(InvalidRequestException.class);
        todoService.create(user, new ListId("someListId"), "some things");
    }

    @Test
    public void create_whenTodoListNotFound_refusesCreate() throws Exception {
        when(mockTodoListRepository.findOne(any())).thenReturn(Optional.empty());

        exception.expect(InvalidRequestException.class);
        todoService.create(user, new ListId("someListId"), "some things");
    }

    @Test
    public void create_whenTodoListFound_whenRepositoryRejectsModels_refusesCreate() throws Exception {
        doThrow(new AbnormalModelException()).when(mockTodoListRepository).save(any());

        exception.expect(InvalidRequestException.class);
        todoService.create(user, new ListId("someListId"), "some things");
    }

    @Test
    public void createDeferred_whenTodoListFound_addsTodoToRepository() throws Exception {
        String task = "some things";
        todoService.createDeferred(user, new ListId("someListId"), task);

        verify(todoList).addDeferred(new TodoId(todoIdentifier), task);
        verify(mockTodoListRepository).save(todoList);
    }

    @Test
    public void createDeferred_whenTodoListFound_whenTodoWithTaskExists_refusesCreate() throws Exception {
        doThrow(new DuplicateTodoException()).when(todoList).addDeferred(any(TodoId.class), any());

        assertThatThrownBy(() ->
            todoService.createDeferred(user, new ListId("someListId"), "some things"))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessageContaining("already exists");

    }

    @Test
    public void createDeferred_whenTodoListNotFound_refusesCreate() throws Exception {
        when(mockTodoListRepository.findOne(any())).thenReturn(Optional.empty());

        exception.expect(InvalidRequestException.class);
        todoService.createDeferred(user, new ListId("someListId"), "some things");
    }

    @Test
    public void createDeferred_whenTodoListFound_whenRepositoryRejectsModels_refusesCreate() throws Exception {
        doThrow(new AbnormalModelException()).when(mockTodoListRepository).save(any());

        exception.expect(InvalidRequestException.class);
        todoService.createDeferred(user, new ListId("someListId"), "some things");
    }

    @Test
    public void delete_whenTodoListFound_whenTodoFound_deletesTodoUsingRepository() throws Exception {
        TodoId todoId = new TodoId("someIdentifier");
        todoService.delete(user, new ListId("someListId"), todoId);

        verify(todoList).delete(todoId);
        verify(mockTodoListRepository).save(todoList);
    }

    @Test
    public void delete_whenTodoListFound_whenTodoFound_whenRepositoryRejectsModels_refusesDelete() throws Exception {
        doThrow(new AbnormalModelException()).when(mockTodoListRepository).save(any());

        exception.expect(InvalidRequestException.class);
        todoService.delete(user, new ListId("someListId"), new TodoId("someTodoId"));
    }

    @Test
    public void delete_whenTodoListFound_whenTodoNotFound_refusesDelete() throws Exception {
        doThrow(new TodoNotFoundException()).when(todoList).delete(any());

        exception.expect(InvalidRequestException.class);
        todoService.delete(user, new ListId("someListId"), new TodoId("someTodoId"));
    }

    @Test
    public void displace_whenTodoListFound_whenTodoFound_savesUsingRepository() throws Exception {
        todoService.displace(user, "someTask");

        verify(todoList).displace(new TodoId(todoIdentifier), "someTask");
        verify(mockTodoListRepository).save(todoList);
    }

    @Test
    public void displace_whenTodoListNotFound_refusesDisplace() throws Exception {
        when(mockTodoListRepository.findOne(any())).thenReturn(Optional.empty());

        exception.expect(InvalidRequestException.class);
        todoService.displace(user, "someTask");
    }

    @Test
    public void displace_whenTodoListFound_whenTodoFound_whenTodoWithTaskExists_refusesDisplace() throws Exception {
        doThrow(new DuplicateTodoException()).when(todoList).displace(any(TodoId.class), any());

        assertThatThrownBy(() ->
            todoService.displace(user, "someTask"))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    public void displace_whenTodoListFound_whenTodoFound_whenListIsNotFull_refusesDisplace() throws Exception {
        doThrow(new ListNotFullException()).when(todoList).displace(any(TodoId.class), any());

        exception.expect(InvalidRequestException.class);
        todoService.displace(user, "someTask");
    }

    @Test
    public void displace_whenTodoListFound_whenTodoFound_whenRepositoryRejectsModel_refusesDisplace() throws Exception {
        doThrow(new AbnormalModelException()).when(mockTodoListRepository).save(any());

        exception.expect(InvalidRequestException.class);
        todoService.displace(user, "someTask");
    }

    @Test
    public void update_whenTodoListFound_whenTodoFound_updatesUsingRepository() throws Exception {
        String updatedTask = "someOtherTask";
        TodoId todoId = new TodoId("someIdentifier");
        todoService.update(user, todoId, updatedTask);

        verify(todoList).update(todoId, updatedTask);
        verify(mockTodoListRepository).save(todoList);
    }

    @Test
    public void update_whenTodoListFound_whenTodoFound_whenRepositoryRejectsModel_refusesOperation() throws Exception {
        doThrow(new AbnormalModelException()).when(mockTodoListRepository).save(any());

        exception.expect(InvalidRequestException.class);
        todoService.update(user, new TodoId("someIdentifier"), "someOtherTask");
    }

    @Test
    public void update_whenTodoListFound_whenTodoFound_whenTodoWithTaskExists_refusesUpdate() throws Exception {
        doThrow(new DuplicateTodoException()).when(todoList).update(any(), any());

        assertThatThrownBy(() ->
            todoService.update(user, new TodoId("someIdentifier"), "someTask"))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    public void update_whenTodoListFound_whenTodoNotFound_refusesUpdate() throws Exception {
        doThrow(new TodoNotFoundException()).when(todoList).update(any(), any());

        exception.expect(InvalidRequestException.class);
        todoService.update(user, new TodoId("someId"), "someTask");
    }

    @Test
    public void update_whenTodoListNotFound_refusesUpdate() throws Exception {
        when(mockTodoListRepository.findOne(any())).thenReturn(Optional.empty());

        exception.expect(InvalidRequestException.class);
        todoService.update(user, new TodoId("someId"), "someTask");
    }

    @Test
    public void complete_whenTodoListFound_whenTodoFound_completesTodo_savesUsingRepository() throws Exception {
        TodoId todoId = new TodoId("someIdentifier");
        todoService.complete(user, new ListId("someListId"), todoId);

        verify(todoList).complete(todoId);
        verify(mockTodoListRepository).save(todoList);
    }

    @Test
    public void complete_addsCompletedTaskToCompletedList_savesUsingRepository() throws Exception {
        CompletedTodo completedTodo = new CompletedTodo(
            new UserId("someUserId"),
            new CompletedTodoId("someIdentifier"),
            "completedTask",
            Date.from(Instant.now()));
        when(todoList.complete(any(TodoId.class))).thenReturn(completedTodo);

        todoService.complete(user, new ListId("someListId"), new TodoId("someIdentifier"));

        verify(mockCompletedTodoRepository).save(completedTodo);
    }

    @Test
    public void complete_whenCompletedListRepositoryRejectsModel_refusesOperation() throws Exception {
        CompletedTodo completedTodo = new CompletedTodo(
            new UserId("someUserId"),
            new CompletedTodoId("someIdentifier"),
            "completedTask",
            Date.from(Instant.now()));
        when(todoList.complete(any(TodoId.class))).thenReturn(completedTodo);
        doThrow(new AbnormalModelException()).when(mockCompletedTodoRepository).save(any(CompletedTodo.class));

        exception.expect(InvalidRequestException.class);
        todoService.complete(user, new ListId("someListId"), new TodoId("someIdentifier"));
    }

    @Test
    public void complete_whenTodoListFound_whenTodoFound_whenRepositoryRejectsModel_refusesOperation() throws Exception {
        doThrow(new AbnormalModelException()).when(mockTodoListRepository).save(any());

        exception.expect(InvalidRequestException.class);
        todoService.complete(user, new ListId("someListId"), new TodoId("someIdentifier"));
    }

    @Test
    public void complete_whenTodoListFound_whenTodoNotFound_refusesUpdate() throws Exception {
        doThrow(new TodoNotFoundException()).when(todoList).complete(any());

        exception.expect(InvalidRequestException.class);
        todoService.complete(user, new ListId("someListId"), new TodoId("someId"));
    }

    @Test
    public void complete_whenTodoListNotFound_refusesUpdate() throws Exception {
        when(mockTodoListRepository.findOne(any())).thenReturn(Optional.empty());

        exception.expect(InvalidRequestException.class);
        todoService.complete(user, new ListId("someListId"), new TodoId("someId"));
    }

    @Test
    public void move_whenTodoListFound_whenTodosFound_updatesMovedTodosUsingRepository() throws Exception {
        TodoId sourceIdentifier = new TodoId("sourceIdentifier");
        TodoId destinationIdentifier = new TodoId("destinationIdentifier");
        todoService.move(user, sourceIdentifier, destinationIdentifier);

        verify(todoList).move(sourceIdentifier, destinationIdentifier);
        verify(mockTodoListRepository).save(todoList);
    }

    @Test
    public void move_whenTodoListFound_whenTodosFound_whenRepositoryRejectsModel_refusesOperation() throws Exception {
        doThrow(new AbnormalModelException()).when(mockTodoListRepository).save(any());

        exception.expect(InvalidRequestException.class);
        todoService.move(user, new TodoId("sourceIdentifier"), new TodoId("destinationIdentifier"));
    }

    @Test
    public void move_whenTodoListFound_whenTodosNotFound_refusesOperation() throws Exception {
        doThrow(new TodoNotFoundException()).when(todoList).move(any(), any());

        exception.expect(InvalidRequestException.class);
        todoService.move(user, new TodoId("idOne"), new TodoId("idTwo"));
    }

    @Test
    public void move_whenTodoListNotFound_refusesOperation() throws Exception {
        when(mockTodoListRepository.findOne(any())).thenReturn(Optional.empty());

        exception.expect(InvalidRequestException.class);
        todoService.move(user, new TodoId("idOne"), new TodoId("idTwo"));
    }

    @Test
    public void pull_whenTodoListFound_whenTodosPulled_updatesPulledTodosUsingRepository() throws Exception {
        todoService.pull(user);

        verify(todoList).pull();
        verify(mockTodoListRepository).save(todoList);
    }

    @Test
    public void pull_whenTodoListFound_whenTodosPulled_whenRepositoryRejectsModel_refusesOperation() throws Exception {
        doThrow(new AbnormalModelException()).when(mockTodoListRepository).save(any());

        exception.expect(InvalidRequestException.class);
        todoService.pull(user);
    }

    @Test
    public void escalate_escalatesList_andSavesIt() throws Exception {
        todoService.escalate(user);

        verify(todoList).escalate();
        verify(mockTodoListRepository).save(todoList);
    }

    @Test
    public void escalate_whenEscalateNotAllowed_refusesOperation() throws Exception {
        doThrow(new EscalateNotAllowException()).when(todoList).escalate();

        exception.expect(InvalidRequestException.class);
        todoService.escalate(user);
    }

    @Test
    public void escalate_whenRepositoryRejectsModel_refusesOperation() throws Exception {
        doThrow(new AbnormalModelException()).when(mockTodoListRepository).save(any());

        exception.expect(InvalidRequestException.class);
        todoService.escalate(user);
    }

    @Test
    public void escalate_whenListNotFound_refusesOperation() throws Exception {
        when(mockTodoListRepository.findOne(any())).thenReturn(Optional.empty());

        exception.expect(InvalidRequestException.class);
        todoService.escalate(user);
    }
}