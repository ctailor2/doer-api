package com.doerapispring.domain;

import com.doerapispring.web.InvalidRequestException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TodoServiceTest {
    private TodoService todoService;

    @Mock
    private IdentityGeneratingObjectRepository<CompletedList, String> mockCompletedListRepository;

    @Mock
    private OwnedObjectRepository<TodoList, UserId, ListId> mockTodoListRepository;

    @Mock
    private IdentityGeneratingRepository<TodoId> mockTodoRepository;

    @Rule
    public final ExpectedException exception = ExpectedException.none();
    private UniqueIdentifier<String> uniqueIdentifier;
    private TodoList todoList;

    private CompletedList completedList;
    private String todoIdentifier;
    private String completedTodoIdentifier;

    @Before
    public void setUp() throws Exception {
        todoService = new TodoService(
            mockCompletedListRepository,
            mockTodoListRepository,
            mockTodoRepository
        );
        uniqueIdentifier = new UniqueIdentifier<>("userId");
        todoList = mock(TodoList.class);
        when(mockTodoListRepository.findOne(any())).thenReturn(Optional.of(todoList));
        todoIdentifier = "todoId";
        completedList = mock(CompletedList.class);
        when(mockCompletedListRepository.find(uniqueIdentifier)).thenReturn(Optional.of(completedList));
        when(mockTodoRepository.nextIdentifier()).thenReturn(new TodoId(todoIdentifier));
        completedTodoIdentifier = "completedTodoId";
        when(mockCompletedListRepository.nextIdentifier()).thenReturn(new UniqueIdentifier<>(completedTodoIdentifier));
    }

    @Test
    public void create_whenTodoListFound_addsTodoToRepository() throws Exception {
        String task = "some things";
        todoService.create(new User(new UserId(uniqueIdentifier.get())), task);

        verify(todoList).add(new TodoId(todoIdentifier), task);
        verify(mockTodoListRepository).save(todoList);
    }

    @Test
    public void create_whenTodoListFound_whenTodoWithTaskExists_refusesCreate() throws Exception {
        doThrow(new DuplicateTodoException()).when(todoList).add(any(TodoId.class), any());

        assertThatThrownBy(() ->
            todoService.create(new User(new UserId(uniqueIdentifier.get())), "some things"))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    public void create_whenTodoListFound_whenListFull_refusesCreate() throws Exception {
        doThrow(new ListSizeExceededException()).when(todoList).add(any(TodoId.class), any());

        exception.expect(InvalidRequestException.class);
        todoService.create(new User(new UserId(uniqueIdentifier.get())), "some things");
    }

    @Test
    public void create_whenTodoListNotFound_refusesCreate() throws Exception {
        when(mockTodoListRepository.findOne(any())).thenReturn(Optional.empty());

        exception.expect(InvalidRequestException.class);
        todoService.create(new User(new UserId(uniqueIdentifier.get())), "some things");
    }

    @Test
    public void create_whenTodoListFound_whenRepositoryRejectsModels_refusesCreate() throws Exception {
        doThrow(new AbnormalModelException()).when(mockTodoListRepository).save(any());

        exception.expect(InvalidRequestException.class);
        todoService.create(new User(new UserId(uniqueIdentifier.get())), "some things");
    }

    @Test
    public void createDeferred_whenTodoListFound_addsTodoToRepository() throws Exception {
        String task = "some things";
        todoService.createDeferred(new User(new UserId(uniqueIdentifier.get())), task);

        verify(todoList).addDeferred(new TodoId(todoIdentifier), task);
        verify(mockTodoListRepository).save(todoList);
    }

    @Test
    public void createDeferred_whenTodoListFound_whenTodoWithTaskExists_refusesCreate() throws Exception {
        doThrow(new DuplicateTodoException()).when(todoList).addDeferred(any(TodoId.class), any());

        assertThatThrownBy(() ->
            todoService.createDeferred(new User(new UserId(uniqueIdentifier.get())), "some things"))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessageContaining("already exists");

    }

    @Test
    public void createDeferred_whenTodoListNotFound_refusesCreate() throws Exception {
        when(mockTodoListRepository.findOne(any())).thenReturn(Optional.empty());

        exception.expect(InvalidRequestException.class);
        todoService.createDeferred(new User(new UserId(uniqueIdentifier.get())), "some things");
    }

    @Test
    public void createDeferred_whenTodoListFound_whenRepositoryRejectsModels_refusesCreate() throws Exception {
        doThrow(new AbnormalModelException()).when(mockTodoListRepository).save(any());

        exception.expect(InvalidRequestException.class);
        todoService.createDeferred(new User(new UserId(uniqueIdentifier.get())), "some things");
    }

    @Test
    public void delete_whenTodoListFound_whenTodoFound_deletesTodoUsingRepository() throws Exception {
        TodoId todoId = new TodoId("someIdentifier");
        todoService.delete(new User(new UserId(uniqueIdentifier.get())), todoId);

        verify(todoList).delete(todoId);
        verify(mockTodoListRepository).save(todoList);
    }

    @Test
    public void delete_whenTodoListFound_whenTodoFound_whenRepositoryRejectsModels_refusesDelete() throws Exception {
        doThrow(new AbnormalModelException()).when(mockTodoListRepository).save(any());

        exception.expect(InvalidRequestException.class);
        todoService.delete(new User(new UserId(uniqueIdentifier.get())), new TodoId("someTodoId"));
    }

    @Test
    public void delete_whenTodoListFound_whenTodoNotFound_refusesDelete() throws Exception {
        doThrow(new TodoNotFoundException()).when(todoList).delete(any());

        exception.expect(InvalidRequestException.class);
        todoService.delete(new User(new UserId(uniqueIdentifier.get())), new TodoId("someTodoId"));
    }

    @Test
    public void displace_whenTodoListFound_whenTodoFound_savesUsingRepository() throws Exception {
        todoService.displace(new User(new UserId(uniqueIdentifier.get())), "someTask");

        verify(todoList).displace(new TodoId(todoIdentifier), "someTask");
        verify(mockTodoListRepository).save(todoList);
    }

    @Test
    public void displace_whenTodoListNotFound_refusesDisplace() throws Exception {
        when(mockTodoListRepository.findOne(any())).thenReturn(Optional.empty());

        exception.expect(InvalidRequestException.class);
        todoService.displace(new User(new UserId(uniqueIdentifier.get())), "someTask");
    }

    @Test
    public void displace_whenTodoListFound_whenTodoFound_whenTodoWithTaskExists_refusesDisplace() throws Exception {
        doThrow(new DuplicateTodoException()).when(todoList).displace(any(TodoId.class), any());

        assertThatThrownBy(() ->
            todoService.displace(new User(new UserId(uniqueIdentifier.get())), "someTask"))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    public void displace_whenTodoListFound_whenTodoFound_whenListIsNotFull_refusesDisplace() throws Exception {
        doThrow(new ListNotFullException()).when(todoList).displace(any(TodoId.class), any());

        exception.expect(InvalidRequestException.class);
        todoService.displace(new User(new UserId(uniqueIdentifier.get())), "someTask");
    }

    @Test
    public void displace_whenTodoListFound_whenTodoFound_whenRepositoryRejectsModel_refusesDisplace() throws Exception {
        doThrow(new AbnormalModelException()).when(mockTodoListRepository).save(any());

        exception.expect(InvalidRequestException.class);
        todoService.displace(new User(new UserId(uniqueIdentifier.get())), "someTask");
    }

    @Test
    public void update_whenTodoListFound_whenTodoFound_updatesUsingRepository() throws Exception {
        String updatedTask = "someOtherTask";
        TodoId todoId = new TodoId("someIdentifier");
        todoService.update(new User(new UserId(uniqueIdentifier.get())), todoId, updatedTask);

        verify(todoList).update(todoId, updatedTask);
        verify(mockTodoListRepository).save(todoList);
    }

    @Test
    public void update_whenTodoListFound_whenTodoFound_whenRepositoryRejectsModel_refusesOperation() throws Exception {
        doThrow(new AbnormalModelException()).when(mockTodoListRepository).save(any());

        exception.expect(InvalidRequestException.class);
        todoService.update(new User(new UserId(uniqueIdentifier.get())), new TodoId("someIdentifier"), "someOtherTask");
    }

    @Test
    public void update_whenTodoListFound_whenTodoFound_whenTodoWithTaskExists_refusesUpdate() throws Exception {
        doThrow(new DuplicateTodoException()).when(todoList).update(any(), any());

        assertThatThrownBy(() ->
            todoService.update(new User(new UserId(uniqueIdentifier.get())), new TodoId("someIdentifier"), "someTask"))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    public void update_whenTodoListFound_whenTodoNotFound_refusesUpdate() throws Exception {
        doThrow(new TodoNotFoundException()).when(todoList).update(any(), any());

        exception.expect(InvalidRequestException.class);
        todoService.update(new User(new UserId(uniqueIdentifier.get())), new TodoId("someId"), "someTask");
    }

    @Test
    public void update_whenTodoListNotFound_refusesUpdate() throws Exception {
        when(mockTodoListRepository.findOne(any())).thenReturn(Optional.empty());

        exception.expect(InvalidRequestException.class);
        todoService.update(new User(new UserId(uniqueIdentifier.get())), new TodoId("someId"), "someTask");
    }

    @Test
    public void complete_whenTodoListFound_whenTodoFound_completesUsingRepository() throws Exception {
        TodoId todoId = new TodoId("someIdentifier");
        todoService.complete(new User(new UserId(uniqueIdentifier.get())), todoId);

        verify(todoList).complete(todoId);
        verify(mockTodoListRepository).save(todoList);
    }

    @Test
    public void complete_addsCompletedTaskToCompletedList_savesUsingRepository() throws Exception {
        String completedTask = "completedTask";
        when(todoList.complete(any(TodoId.class))).thenReturn(completedTask);

        todoService.complete(new User(new UserId(uniqueIdentifier.get())), new TodoId("someIdentifier"));

        verify(mockCompletedListRepository).find(uniqueIdentifier);
        verify(completedList).add(new CompletedTodoId(completedTodoIdentifier), completedTask);
        verify(mockCompletedListRepository).save(completedList);
    }

    @Test
    public void complete_whenCompletedListRepositoryRejectsModel_refusesOperation() throws Exception {
        String completedTask = "completedTask";
        when(todoList.complete(any(TodoId.class))).thenReturn(completedTask);
        doThrow(new AbnormalModelException()).when(mockCompletedListRepository).save(any(CompletedList.class));

        exception.expect(InvalidRequestException.class);
        todoService.complete(new User(new UserId(uniqueIdentifier.get())), new TodoId("someIdentifier"));
    }

    @Test
    public void complete_whenTodoListFound_whenTodoFound_whenRepositoryRejectsModel_refusesOperation() throws Exception {
        doThrow(new AbnormalModelException()).when(mockTodoListRepository).save(any());

        exception.expect(InvalidRequestException.class);
        todoService.complete(new User(new UserId(uniqueIdentifier.get())), new TodoId("someIdentifier"));
    }

    @Test
    public void complete_whenTodoListFound_whenTodoNotFound_refusesUpdate() throws Exception {
        doThrow(new TodoNotFoundException()).when(todoList).complete(any());

        exception.expect(InvalidRequestException.class);
        todoService.complete(new User(new UserId(uniqueIdentifier.get())), new TodoId("someId"));
    }

    @Test
    public void complete_whenTodoListNotFound_refusesUpdate() throws Exception {
        when(mockTodoListRepository.findOne(any())).thenReturn(Optional.empty());

        exception.expect(InvalidRequestException.class);
        todoService.complete(new User(new UserId(uniqueIdentifier.get())), new TodoId("someId"));
    }

    @Test
    public void move_whenTodoListFound_whenTodosFound_updatesMovedTodosUsingRepository() throws Exception {
        TodoId sourceIdentifier = new TodoId("sourceIdentifier");
        TodoId destinationIdentifier = new TodoId("destinationIdentifier");
        todoService.move(new User(new UserId(uniqueIdentifier.get())), sourceIdentifier, destinationIdentifier);

        verify(todoList).move(sourceIdentifier, destinationIdentifier);
        verify(mockTodoListRepository).save(todoList);
    }

    @Test
    public void move_whenTodoListFound_whenTodosFound_whenRepositoryRejectsModel_refusesOperation() throws Exception {
        doThrow(new AbnormalModelException()).when(mockTodoListRepository).save(any());

        exception.expect(InvalidRequestException.class);
        todoService.move(new User(new UserId(uniqueIdentifier.get())), new TodoId("sourceIdentifier"), new TodoId("destinationIdentifier"));
    }

    @Test
    public void move_whenTodoListFound_whenTodosNotFound_refusesOperation() throws Exception {
        doThrow(new TodoNotFoundException()).when(todoList).move(any(), any());

        exception.expect(InvalidRequestException.class);
        todoService.move(new User(new UserId(uniqueIdentifier.get())), new TodoId("idOne"), new TodoId("idTwo"));
    }

    @Test
    public void move_whenTodoListNotFound_refusesOperation() throws Exception {
        when(mockTodoListRepository.findOne(any())).thenReturn(Optional.empty());

        exception.expect(InvalidRequestException.class);
        todoService.move(new User(new UserId(uniqueIdentifier.get())), new TodoId("idOne"), new TodoId("idTwo"));
    }

    @Test
    public void pull_whenTodoListFound_whenTodosPulled_updatesPulledTodosUsingRepository() throws Exception {
        todoService.pull(new User(new UserId(uniqueIdentifier.get())));

        verify(todoList).pull();
        verify(mockTodoListRepository).save(todoList);
    }

    @Test
    public void pull_whenTodoListFound_whenTodosPulled_whenRepositoryRejectsModel_refusesOperation() throws Exception {
        doThrow(new AbnormalModelException()).when(mockTodoListRepository).save(any());

        exception.expect(InvalidRequestException.class);
        todoService.pull(new User(new UserId(uniqueIdentifier.get())));
    }
}