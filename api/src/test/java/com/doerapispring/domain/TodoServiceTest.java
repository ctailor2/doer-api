package com.doerapispring.domain;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TodoServiceTest {
    private TodoService todoService;

    private ListService listService;

    @Mock
    private AggregateRootRepository<MasterList, Todo> mockTodoRepository;

    @Mock
    private ObjectRepository<CompletedList, String> mockCompletedListRepository;

    @Rule
    public final ExpectedException exception = ExpectedException.none();
    private UniqueIdentifier<String> uniqueIdentifier;
    private MasterList masterList;

    private final ArgumentCaptor<Todo> todoArgumentCaptor = ArgumentCaptor.forClass(Todo.class);

    @Captor
    private ArgumentCaptor<List<Todo>> todoListArgumentCaptor;

    @Before
    public void setUp() throws Exception {
        listService = mock(ListService.class);
        todoService = new TodoService(
            listService,
            mockTodoRepository,
            mockCompletedListRepository
        );
        uniqueIdentifier = new UniqueIdentifier<>("userId");
        masterList = new MasterList(Clock.systemDefaultZone(), uniqueIdentifier, new ArrayList<>());
        when(listService.get(any())).thenReturn(masterList);
    }

    @Test
    public void get_whenMasterListFound_returnsMasterListFromRepository() throws Exception {
        when(listService.get(any())).thenReturn(masterList);
        User user = new User(uniqueIdentifier);

        MasterList masterList = todoService.get(user);

        verify(listService).get(user);
        assertThat(masterList).isEqualTo(masterList);
    }

    @Test
    public void get_whenMasterListNotFound_refusesGet() throws Exception {
        when(listService.get(any())).thenThrow(new OperationRefusedException());

        exception.expect(OperationRefusedException.class);
        todoService.get(new User(new UniqueIdentifier<>("one@two.com")));
    }

    @Test
    public void getDeferredTodos_whenMasterListFound_returnsDeferredTodos() throws Exception {
        Todo todo = masterList.addDeferred("someTask");
        masterList.unlock();

        User user = new User(uniqueIdentifier);
        List<Todo> todos = todoService.getDeferredTodos(user);

        verify(listService).get(user);
        assertThat(todos).containsOnly(todo);
    }

    @Test
    public void getDeferredTodos_whenMasterListFound_whenLockTimerNotExpired_refusesOperation() throws Exception {
        exception.expect(OperationRefusedException.class);
        User user = new User(uniqueIdentifier);
        todoService.getDeferredTodos(user);
    }

    @Test
    public void getDeferredTodos_whenMasterListNotFound_refusesGet() throws Exception {
        when(listService.get(any())).thenThrow(new OperationRefusedException());

        exception.expect(OperationRefusedException.class);
        todoService.getDeferredTodos(new User(uniqueIdentifier));
    }

    @Test
    public void create_whenMasterListFound_addsTodoToRepository() throws Exception {
        String task = "some things";
        todoService.create(new User(uniqueIdentifier), task);

        verify(mockTodoRepository).add(eq(masterList), todoArgumentCaptor.capture());
        Todo todo = todoArgumentCaptor.getValue();
        assertThat(todo.getTask()).isEqualTo(task);
        assertThat(todo.getListName()).isEqualTo(MasterList.NAME);
    }

    @Test
    public void create_whenMasterListFound_whenTodoWithTaskExists_refusesCreate() throws Exception {
        masterList.add("some things");

        assertThatThrownBy(() ->
            todoService.create(new User(uniqueIdentifier), "some things"))
            .isInstanceOf(OperationRefusedException.class)
            .hasMessageContaining("already exists");

    }

    @Test
    public void create_whenMasterListFound_whenListFull_refusesCreate() throws Exception {
        masterList.add("task1");
        masterList.add("task2");

        exception.expect(OperationRefusedException.class);
        todoService.create(new User(uniqueIdentifier), "some things");
    }

    @Test
    public void create_whenMasterListNotFound_refusesCreate() throws Exception {
        when(listService.get(any())).thenThrow(new OperationRefusedException());

        exception.expect(OperationRefusedException.class);
        todoService.create(new User(uniqueIdentifier), "some things");
    }

    @Test
    public void create_whenMasterListFound_whenRepositoryRejectsModels_refusesCreate() throws Exception {
        doThrow(new AbnormalModelException()).when(mockTodoRepository).add(any(), any());

        exception.expect(OperationRefusedException.class);
        todoService.create(new User(uniqueIdentifier), "some things");
    }

    @Test
    public void createDeferred_whenMasterListFound_addsTodoToRepository() throws Exception {
        String task = "some things";
        todoService.createDeferred(new User(uniqueIdentifier), task);

        verify(mockTodoRepository).add(eq(masterList), todoArgumentCaptor.capture());
        Todo todo = todoArgumentCaptor.getValue();
        assertThat(todo.getTask()).isEqualTo(task);
        assertThat(todo.getListName()).isEqualTo(MasterList.DEFERRED_NAME);
    }

    @Test
    public void createDeferred_whenMasterListFound_whenTodoWithTaskExists_refusesCreate() throws Exception {
        masterList.addDeferred("some things");

        assertThatThrownBy(() ->
            todoService.create(new User(uniqueIdentifier), "some things"))
            .isInstanceOf(OperationRefusedException.class)
            .hasMessageContaining("already exists");

    }

    @Test
    public void createDeferred_whenMasterListNotFound_refusesCreate() throws Exception {
        when(listService.get(any())).thenThrow(new OperationRefusedException());

        exception.expect(OperationRefusedException.class);
        todoService.createDeferred(new User(uniqueIdentifier), "some things");
    }

    @Test
    public void createDeferred_whenMasterListFound_whenRepositoryRejectsModels_refusesCreate() throws Exception {
        doThrow(new AbnormalModelException()).when(mockTodoRepository).add(any(), any());

        exception.expect(OperationRefusedException.class);
        todoService.createDeferred(new User(uniqueIdentifier), "some things");
    }

    @Test
    public void delete_whenMasterListFound_whenTodoFound_deletesTodoUsingRepository() throws Exception {
        Todo existingTodo = masterList.add("someTask");

        todoService.delete(new User(uniqueIdentifier), existingTodo.getLocalIdentifier());

        verify(mockTodoRepository).remove(eq(masterList), todoArgumentCaptor.capture());
        Todo todo = todoArgumentCaptor.getValue();
        assertThat(todo.getLocalIdentifier()).isEqualTo(existingTodo.getLocalIdentifier());
        assertThat(todo.getTask()).isEqualTo(existingTodo.getTask());
        assertThat(todo.getListName()).isEqualTo(existingTodo.getListName());
        assertThat(todo.getPosition()).isEqualTo(existingTodo.getPosition());
    }

    @Test
    public void delete_whenMasterListFound_whenTodoFound_whenRepositoryRejectsModels_refusesDelete() throws Exception {
        doThrow(new AbnormalModelException()).when(mockTodoRepository).remove(any(), any());

        exception.expect(OperationRefusedException.class);
        todoService.delete(new User(uniqueIdentifier), "someTodoId");
    }

    @Test
    public void delete_whenMasterListFound_whenTodoNotFound_refusesDelete() throws Exception {
        exception.expect(OperationRefusedException.class);
        todoService.delete(new User(uniqueIdentifier), "someTodoId");

        verifyZeroInteractions(mockTodoRepository);
    }

    @Test
    public void displace_whenMasterListFound_whenTodoFound_addsAndUpdatesUsingRepository() throws Exception {
        Todo existingTodo = masterList.add("someTask");
        masterList.add("someOtherTask");
        Todo firstDeferredTask = masterList.addDeferred("someDeferredTask");

        String displacingTask = "displacingTask";
        todoService.displace(new User(uniqueIdentifier), existingTodo.getLocalIdentifier(), displacingTask);

        verify(mockTodoRepository).update(eq(masterList), todoArgumentCaptor.capture());
        verify(mockTodoRepository).add(eq(masterList), todoArgumentCaptor.capture());
        List<Todo> allCapturedTodos = todoArgumentCaptor.getAllValues();
        Todo updatedTodo = allCapturedTodos.get(0);
        assertThat(updatedTodo.getTask()).isEqualTo(existingTodo.getTask());
        assertThat(updatedTodo.getPosition()).isEqualTo(firstDeferredTask.getPosition() - 1);
        assertThat(updatedTodo.getListName()).isEqualTo(MasterList.DEFERRED_NAME);

        Todo addedTodo = allCapturedTodos.get(1);
        assertThat(addedTodo.getTask()).isEqualTo(displacingTask);
        assertThat(addedTodo.getPosition()).isEqualTo(existingTodo.getPosition());
        assertThat(addedTodo.getListName()).isEqualTo(MasterList.NAME);
    }

    @Test
    public void displace_whenMasterListNotFound_refusesDisplace() throws Exception {
        when(listService.get(any())).thenThrow(new OperationRefusedException());

        exception.expect(OperationRefusedException.class);
        todoService.displace(new User(uniqueIdentifier), "someId", "someTask");
    }

    @Test
    public void displace_whenMasterListFound_whenTodoNotFound_refusesDisplace() throws Exception {
        exception.expect(OperationRefusedException.class);
        todoService.displace(new User(uniqueIdentifier), "someId", "someTask");
    }

    @Test
    public void displace_whenMasterListFound_whenTodoFound_whenTodoWithTaskExists_refusesDisplace() throws Exception {
        Todo todo = masterList.add("someTask");

        assertThatThrownBy(() ->
            todoService.displace(new User(uniqueIdentifier), todo.getLocalIdentifier(), "someTask"))
            .isInstanceOf(OperationRefusedException.class)
            .hasMessageContaining("already exists");

    }

    @Test
    public void displace_whenMasterListFound_whenTodoFound_whenRepositoryRejectsModel_onAdd_refusesDisplace() throws Exception {
        doThrow(new AbnormalModelException()).when(mockTodoRepository).add(any(), any());

        exception.expect(OperationRefusedException.class);
        todoService.displace(new User(uniqueIdentifier), "someId", "someTask");
    }

    @Test
    public void displace_whenMasterListFound_whenTodoFound_whenRepositoryRejectsModel_onUpdate_refusesDisplace() throws Exception {
        doThrow(new AbnormalModelException()).when(mockTodoRepository).update(any(), any(Todo.class));

        exception.expect(OperationRefusedException.class);
        todoService.displace(new User(uniqueIdentifier), "someId", "someTask");
    }

    @Test
    public void update_whenMasterListFound_whenTodoFound_updatesUsingRepository() throws Exception {
        Todo existingTodo = masterList.add("someTask");

        String updatedTask = "someOtherTask";
        todoService.update(new User(uniqueIdentifier), existingTodo.getLocalIdentifier(), updatedTask);

        verify(mockTodoRepository).update(eq(masterList), todoArgumentCaptor.capture());

        Todo updatedTodo = todoArgumentCaptor.getValue();
        assertThat(updatedTodo.getTask()).isEqualTo(updatedTask);
        assertThat(updatedTodo.getLocalIdentifier()).isEqualTo(existingTodo.getLocalIdentifier());
        assertThat(updatedTodo.getPosition()).isEqualTo(existingTodo.getPosition());
        assertThat(updatedTodo.getListName()).isEqualTo(existingTodo.getListName());
    }

    @Test
    public void update_whenMasterListFound_whenTodoFound_whenRepositoryRejectsModel_refusesOperation() throws Exception {
        Todo existingTodo = masterList.add("someTask");
        doThrow(new AbnormalModelException()).when(mockTodoRepository).update(any(), any(Todo.class));

        exception.expect(OperationRefusedException.class);
        todoService.update(new User(uniqueIdentifier), existingTodo.getLocalIdentifier(), "someOtherTask");
    }

    @Test
    public void update_whenMasterListFound_whenTodoFound_whenTodoWithTaskExists_refusesUpdate() throws Exception {
        Todo todo = masterList.add("someTask");

        assertThatThrownBy(() ->
            todoService.update(new User(uniqueIdentifier), todo.getLocalIdentifier(), "someTask"))
            .isInstanceOf(OperationRefusedException.class)
            .hasMessageContaining("already exists");
    }

    @Test
    public void update_whenMasterListFound_whenTodoNotFound_refusesUpdate() throws Exception {
        exception.expect(OperationRefusedException.class);
        todoService.update(new User(uniqueIdentifier), "someId", "someTask");
    }

    @Test
    public void update_whenMasterListNotFound_refusesUpdate() throws Exception {
        when(listService.get(any())).thenThrow(new OperationRefusedException());

        exception.expect(OperationRefusedException.class);
        todoService.update(new User(uniqueIdentifier), "someId", "someTask");
    }

    @Test
    public void complete_whenMasterListFound_whenTodoFound_completesUsingRepository() throws Exception {
        Todo existingTodo = masterList.add("someTask");

        todoService.complete(new User(uniqueIdentifier), existingTodo.getLocalIdentifier());

        verify(mockTodoRepository).update(eq(masterList), todoArgumentCaptor.capture());
        Todo updatedTodo = todoArgumentCaptor.getValue();
        assertThat(updatedTodo.isComplete()).isEqualTo(true);
    }

    @Test
    public void complete_whenMasterListFound_whenTodoFound_whenRepositoryRejectsModel_refusesOperation() throws Exception {
        Todo existingTodo = masterList.add("someTask");
        doThrow(new AbnormalModelException()).when(mockTodoRepository).update(any(), any(Todo.class));

        exception.expect(OperationRefusedException.class);
        todoService.complete(new User(uniqueIdentifier), existingTodo.getLocalIdentifier());
    }

    @Test
    public void complete_whenMasterListFound_whenTodoNotFound_refusesUpdate() throws Exception {
        exception.expect(OperationRefusedException.class);
        todoService.complete(new User(uniqueIdentifier), "someId");
    }

    @Test
    public void complete_whenMasterListNotFound_refusesUpdate() throws Exception {
        when(listService.get(any())).thenThrow(new OperationRefusedException());

        exception.expect(OperationRefusedException.class);
        todoService.complete(new User(uniqueIdentifier), "someId");
    }

    @Test
    public void getCompleted_whenCompletedListFound_returnsCompletedListFromRepository() throws Exception {
        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>("one@two.com");
        CompletedList completedListFromRepository = new CompletedList(uniqueIdentifier, emptyList());
        when(mockCompletedListRepository.find(any())).thenReturn(Optional.of(completedListFromRepository));
        User user = new User(uniqueIdentifier);

        CompletedList completedList = todoService.getCompleted(user);

        verify(mockCompletedListRepository).find(uniqueIdentifier);
        assertThat(completedList).isEqualTo(completedListFromRepository);
    }

    @Test
    public void getCompleted_whenCompletedListNotFound_refusesGet() throws Exception {
        when(mockCompletedListRepository.find(any())).thenReturn(Optional.empty());

        exception.expect(OperationRefusedException.class);
        todoService.getCompleted(new User(new UniqueIdentifier<>("one@two.com")));
    }

    @Test
    public void move_whenMasterListFound_whenTodosFound_updatesMovedTodosUsingRepository() throws Exception {
        Todo todo1 = masterList.add("task1");
        Todo todo2 = masterList.add("task2");

        todoService.move(new User(uniqueIdentifier), todo1.getLocalIdentifier(), todo2.getLocalIdentifier());

        verify(mockTodoRepository).update(eq(masterList), todoListArgumentCaptor.capture());
        List<Todo> updatedTodos = todoListArgumentCaptor.getValue();
        assertThat(updatedTodos).hasSize(2);
        Todo updatedTodo1 = updatedTodos.get(0);
        assertThat(updatedTodo1.getTask()).isEqualTo(todo2.getTask());
        assertThat(updatedTodo1.getPosition()).isEqualTo(todo2.getPosition());
        Todo updatedTodo2 = updatedTodos.get(1);
        assertThat(updatedTodo2.getTask()).isEqualTo(todo1.getTask());
        assertThat(updatedTodo2.getPosition()).isEqualTo(todo1.getPosition());
    }

    @Test
    public void move_whenMasterListFound_whenTodosFound_whenRepositoryRejectsModel_refusesOperation() throws Exception {
        Todo todo1 = masterList.add("task1");
        Todo todo2 = masterList.add("task2");

        doThrow(new AbnormalModelException()).when(mockTodoRepository).update(any(), anyListOf(Todo.class));

        exception.expect(OperationRefusedException.class);
        todoService.move(new User(uniqueIdentifier), todo1.getLocalIdentifier(), todo2.getLocalIdentifier());
    }

    @Test
    public void move_whenMasterListFound_whenTodosNotFound_refusesOperation() throws Exception {
        exception.expect(OperationRefusedException.class);
        todoService.move(new User(uniqueIdentifier), "idOne", "idTwo");
    }

    @Test
    public void move_whenMasterListNotFound_refsusesOperation() throws Exception {
        when(listService.get(any())).thenThrow(new OperationRefusedException());

        exception.expect(OperationRefusedException.class);
        todoService.move(new User(uniqueIdentifier), "idOne", "idTwo");
    }

    @Test
    public void pull_whenMasterListFound_whenTodosPulled_updatesPulledTodosUsingRepository() throws Exception {
        Todo todo1 = masterList.addDeferred("someTask");
        Todo todo2 = masterList.addDeferred("someOtherTask");

        todoService.pull(new User(uniqueIdentifier));

        verify(mockTodoRepository).update(eq(masterList), todoListArgumentCaptor.capture());
        List<Todo> pulledTodos = todoListArgumentCaptor.getValue();
        assertThat(pulledTodos).hasSize(2);
        Todo pulledTodo1 = pulledTodos.get(0);
        assertThat(pulledTodo1.getTask()).isEqualTo(todo1.getTask());
        assertThat(pulledTodo1.getListName()).isEqualTo(MasterList.NAME);
        Todo pulledTodo2 = pulledTodos.get(1);
        assertThat(pulledTodo2.getTask()).isEqualTo(todo2.getTask());
        assertThat(pulledTodo2.getListName()).isEqualTo(MasterList.NAME);
    }

    @Test
    public void pull_whenMasterListFound_whenTodosPulled_whenRepositoryRejectsModel_refusesOperation() throws Exception {
        doThrow(new AbnormalModelException()).when(mockTodoRepository).update(any(), anyListOf(Todo.class));

        exception.expect(OperationRefusedException.class);
        todoService.pull(new User(uniqueIdentifier));
    }
}