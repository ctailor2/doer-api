package com.doerapispring.domain;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TodoServiceTest {
    private TodoService todoService;

    @Mock
    private AggregateRootRepository<MasterList, Todo, String> mockMasterListRepository;

    @Mock
    private AggregateRootRepository<CompletedList, CompletedTodo, String> mockCompletedListRepository;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        todoService = new TodoService(mockMasterListRepository, mockCompletedListRepository);
    }

    @Test
    public void get_whenMasterListFound_returnsMasterListFromRepository() throws Exception {
        UniqueIdentifier uniqueIdentifier = new UniqueIdentifier<>("one@two.com");
        MasterList masterListFromRepository = new MasterList(uniqueIdentifier, 2);
        when(mockMasterListRepository.find(any())).thenReturn(Optional.of(masterListFromRepository));
        User user = new User(uniqueIdentifier);

        MasterList masterList = todoService.get(user);

        verify(mockMasterListRepository).find(uniqueIdentifier);
        assertThat(masterList).isEqualTo(masterListFromRepository);
    }

    @Test
    public void get_whenMasterListNotFound_refusesGet() throws Exception {
        when(mockMasterListRepository.find(any())).thenReturn(Optional.empty());

        exception.expect(OperationRefusedException.class);
        todoService.get(new User(new UniqueIdentifier<>("one@two.com")));
    }

    @Test
    public void create_whenMasterListFound_addsTodoToRepository() throws Exception {
        MasterList mockMasterList = mock(MasterList.class);
        when(mockMasterListRepository.find(any())).thenReturn(Optional.of(mockMasterList));
        Todo todo = new Todo("some things", ScheduledFor.now, 5);
        when(mockMasterList.add(any(), any())).thenReturn(todo);

        todoService.create(new User(new UniqueIdentifier<>("testItUp")), "some things", ScheduledFor.now);

        verify(mockMasterList).add("some things", ScheduledFor.now);
        verify(mockMasterListRepository).add(mockMasterList, todo);
    }

    @Test
    public void create_whenMasterListFound_whenTodoWithTask_refusesCreate() throws Exception {
        MasterList mockMasterList = mock(MasterList.class);
        when(mockMasterListRepository.find(any())).thenReturn(Optional.of(mockMasterList));
        when(mockMasterList.add(any(), any())).thenThrow(new DuplicateTodoException());

        exception.expect(OperationRefusedException.class);
        todoService.create(new User(new UniqueIdentifier<>("testItUp")), "some things", ScheduledFor.now);
    }

    @Test
    public void create_whenMasterListFound_whenListFull_refusesCreate() throws Exception {
        MasterList mockMasterList = mock(MasterList.class);
        when(mockMasterListRepository.find(any())).thenReturn(Optional.of(mockMasterList));
        when(mockMasterList.add(any(), any())).thenThrow(new ListSizeExceededException());

        exception.expect(OperationRefusedException.class);
        todoService.create(new User(new UniqueIdentifier<>("testItUp")), "some things", ScheduledFor.now);
    }

    @Test
    public void create_whenMasterListNotFound_refusesCreate() throws Exception {
        when(mockMasterListRepository.find(any())).thenReturn(Optional.empty());

        exception.expect(OperationRefusedException.class);
        todoService.create(new User(new UniqueIdentifier<>("testItUp")), "some things", ScheduledFor.now);
    }

    @Test
    public void create_whenMasterListFound_whenRepositoryRejectsModels_refusesCreate() throws Exception {
        MasterList masterList = mock(MasterList.class);
        when(mockMasterListRepository.find(any())).thenReturn(Optional.of(masterList));
        doThrow(new AbnormalModelException()).when(mockMasterListRepository).add(any(), any());

        exception.expect(OperationRefusedException.class);
        todoService.create(new User(new UniqueIdentifier<>("testItUp")), "some things", ScheduledFor.now);
    }

    @Test
    public void delete_whenMasterListFound_whenTodoFound_deletesTodoUsingRepository() throws Exception {
        MasterList mockMasterList = mock(MasterList.class);
        when(mockMasterListRepository.find(any())).thenReturn(Optional.of(mockMasterList));
        Todo todo = new Todo("tasky", ScheduledFor.now, 5);
        when(mockMasterList.delete(any())).thenReturn(todo);

        todoService.delete(new User(new UniqueIdentifier<>("userId")), "someTodoId");

        verify(mockMasterList).delete("someTodoId");
        verify(mockMasterListRepository).remove(mockMasterList, todo);
    }

    @Test
    public void delete_whenMasterListFound_whenTodoFound_whenRepositoryRejectsModels_refusesDelete() throws Exception {
        MasterList mockMasterList = mock(MasterList.class);
        when(mockMasterListRepository.find(any())).thenReturn(Optional.of(mockMasterList));
        Todo todo = new Todo("tasky", ScheduledFor.now, 5);
        when(mockMasterList.delete(any())).thenReturn(todo);
        doThrow(new AbnormalModelException()).when(mockMasterListRepository).remove(any(), any());

        exception.expect(OperationRefusedException.class);
        todoService.delete(new User(new UniqueIdentifier<>("userId")), "someTodoId");
    }

    @Test
    public void delete_whenMasterListFound_whenTodoNotFound_refusesDelete() throws Exception {
        MasterList mockMasterList = mock(MasterList.class);
        when(mockMasterListRepository.find(any())).thenReturn(Optional.of(mockMasterList));
        when(mockMasterList.delete(any())).thenThrow(new TodoNotFoundException());

        exception.expect(OperationRefusedException.class);
        todoService.delete(new User(new UniqueIdentifier<>("userId")), "someTodoId");

        verifyZeroInteractions(mockMasterListRepository);
    }

    @Test
    public void displace_whenMasterListFound_whenTodoFound_addsAndUpdatesUsingRepository() throws Exception {
        MasterList mockMasterList = mock(MasterList.class);
        when(mockMasterListRepository.find(any())).thenReturn(Optional.of(mockMasterList));
        Todo firstTodo = new Todo("tisket", ScheduledFor.now, 5);
        Todo secondTodo = new Todo("tasket", ScheduledFor.now, 3);
        List<Todo> todos = asList(firstTodo, secondTodo);
        when(mockMasterList.displace(any(), any())).thenReturn(todos);

        todoService.displace(new User(new UniqueIdentifier<>("userId")), "someId", "someTask");

        verify(mockMasterList).displace("someId", "someTask");
        verify(mockMasterListRepository).add(mockMasterList, firstTodo);
        verify(mockMasterListRepository).update(mockMasterList, secondTodo);
    }

    @Test
    public void displace_whenMasterListNotFound_refusesDisplace() throws Exception {
        when(mockMasterListRepository.find(any())).thenReturn(Optional.empty());

        exception.expect(OperationRefusedException.class);
        todoService.displace(new User(new UniqueIdentifier<>("userId")), "someId", "someTask");
    }

    @Test
    public void displace_whenMasterListFound_whenTodoNotFound_refusesDisplace() throws Exception {
        MasterList mockMasterList = mock(MasterList.class);
        when(mockMasterListRepository.find(any())).thenReturn(Optional.of(mockMasterList));
        when(mockMasterList.displace(any(), any())).thenThrow(new TodoNotFoundException());

        exception.expect(OperationRefusedException.class);
        todoService.displace(new User(new UniqueIdentifier<>("userId")), "someId", "someTask");
    }

    @Test
    public void displace_whenMasterListFound_whenTodoFound_whenTodoWithTaskExists_refusesDisplace() throws Exception {
        MasterList mockMasterList = mock(MasterList.class);
        when(mockMasterListRepository.find(any())).thenReturn(Optional.of(mockMasterList));
        when(mockMasterList.displace(any(), any())).thenThrow(DuplicateTodoException.class);

        exception.expect(OperationRefusedException.class);
        todoService.displace(new User(new UniqueIdentifier<>("userId")), "someId", "someTask");
    }

    @Test
    public void displace_whenMasterListFound_whenTodoFound_whenRepositoryRejectsModel_onAdd_refusesDisplace() throws Exception {
        MasterList mockMasterList = mock(MasterList.class);
        when(mockMasterListRepository.find(any())).thenReturn(Optional.of(mockMasterList));
        Todo firstTodo = new Todo("tisket", ScheduledFor.now, 5);
        Todo secondTodo = new Todo("tasket", ScheduledFor.now, 3);
        List<Todo> todos = asList(firstTodo, secondTodo);
        when(mockMasterList.displace(any(), any())).thenReturn(todos);
        doThrow(new AbnormalModelException()).when(mockMasterListRepository).add(any(), any());

        exception.expect(OperationRefusedException.class);
        todoService.displace(new User(new UniqueIdentifier<>("userId")), "someId", "someTask");
    }

    @Test
    public void displace_whenMasterListFound_whenTodoFound_whenRepositoryRejectsModel_onUpdate_refusesDisplace() throws Exception {
        MasterList mockMasterList = mock(MasterList.class);
        when(mockMasterListRepository.find(any())).thenReturn(Optional.of(mockMasterList));
        Todo firstTodo = new Todo("tisket", ScheduledFor.now, 5);
        Todo secondTodo = new Todo("tasket", ScheduledFor.now, 3);
        List<Todo> todos = asList(firstTodo, secondTodo);
        when(mockMasterList.displace(any(), any())).thenReturn(todos);
        doThrow(new AbnormalModelException()).when(mockMasterListRepository).update(any(), any(Todo.class));

        exception.expect(OperationRefusedException.class);
        todoService.displace(new User(new UniqueIdentifier<>("userId")), "someId", "someTask");
    }

    @Test
    public void update_whenMasterListFound_whenTodoFound_updatesUsingRepository() throws Exception {
        MasterList mockMasterList = mock(MasterList.class);
        when(mockMasterListRepository.find(any())).thenReturn(Optional.of(mockMasterList));
        Todo todo = new Todo("tisket", ScheduledFor.now, 5);
        when(mockMasterList.update(any(), any())).thenReturn(todo);

        todoService.update(new User(new UniqueIdentifier<>("userId")), "someId", "someTask");

        verify(mockMasterList).update("someId", "someTask");
        verify(mockMasterListRepository).update(mockMasterList, todo);
    }

    @Test
    public void update_whenMasterListFound_whenTodoFound_whenRepositoryRejectsModel_refusesOperation() throws Exception {
        MasterList mockMasterList = mock(MasterList.class);
        when(mockMasterListRepository.find(any())).thenReturn(Optional.of(mockMasterList));
        Todo todo = new Todo("tisket", ScheduledFor.now, 5);
        when(mockMasterList.update(any(), any())).thenReturn(todo);
        doThrow(new AbnormalModelException()).when(mockMasterListRepository).update(any(), any(Todo.class));

        exception.expect(OperationRefusedException.class);
        todoService.update(new User(new UniqueIdentifier<>("userId")), "someId", "someTask");
    }

    @Test
    public void update_whenMasterListFound_whenTodoFound_whenTodoWithTaskExists_refusesUpdate() throws Exception {
        MasterList mockMasterList = mock(MasterList.class);
        when(mockMasterListRepository.find(any())).thenReturn(Optional.of(mockMasterList));
        when(mockMasterList.update(any(), any())).thenThrow(new DuplicateTodoException());

        exception.expect(OperationRefusedException.class);
        todoService.update(new User(new UniqueIdentifier<>("userId")), "someId", "someTask");
    }

        @Test
    public void update_whenMasterListFound_whenTodoNotFound_refusesUpdate() throws Exception {
        MasterList mockMasterList = mock(MasterList.class);
        when(mockMasterListRepository.find(any())).thenReturn(Optional.of(mockMasterList));
        when(mockMasterList.update(any(), any())).thenThrow(new TodoNotFoundException());

        exception.expect(OperationRefusedException.class);
        todoService.update(new User(new UniqueIdentifier<>("userId")), "someId", "someTask");
    }

    @Test
    public void update_whenMasterListNotFound_refusesUpdate() throws Exception {
        when(mockMasterListRepository.find(any())).thenReturn(Optional.empty());

        exception.expect(OperationRefusedException.class);
        todoService.update(new User(new UniqueIdentifier<>("userId")), "someId", "someTask");
    }

    @Test
    public void complete_whenMasterListFound_whenTodoFound_completesUsingRepository() throws Exception {
        MasterList mockMasterList = mock(MasterList.class);
        when(mockMasterListRepository.find(any())).thenReturn(Optional.of(mockMasterList));
        Todo todo = new Todo("tisket", ScheduledFor.now, 5);
        when(mockMasterList.complete(any())).thenReturn(todo);

        todoService.complete(new User(new UniqueIdentifier<>("userId")), "someId");

        verify(mockMasterList).complete("someId");
        verify(mockMasterListRepository).update(mockMasterList, todo);
    }

    @Test
    public void complete_whenMasterListFound_whenTodoFound_whenRepositoryRejectsModel_refusesOperation() throws Exception {
        MasterList mockMasterList = mock(MasterList.class);
        when(mockMasterListRepository.find(any())).thenReturn(Optional.of(mockMasterList));
        Todo todo = new Todo("tisket", ScheduledFor.now, 5);
        when(mockMasterList.complete(any())).thenReturn(todo);
        doThrow(new AbnormalModelException()).when(mockMasterListRepository).update(any(), any(Todo.class));

        exception.expect(OperationRefusedException.class);
        todoService.complete(new User(new UniqueIdentifier<>("userId")), "someId");
    }

    @Test
    public void complete_whenMasterListFound_whenTodoNotFound_refusesUpdate() throws Exception {
        MasterList mockMasterList = mock(MasterList.class);
        when(mockMasterListRepository.find(any())).thenReturn(Optional.of(mockMasterList));
        when(mockMasterList.complete(any())).thenThrow(new TodoNotFoundException());

        exception.expect(OperationRefusedException.class);
        todoService.complete(new User(new UniqueIdentifier<>("userId")), "someId");
    }

    @Test
    public void complete_whenMasterListNotFound_refusesUpdate() throws Exception {
        when(mockMasterListRepository.find(any())).thenReturn(Optional.empty());

        exception.expect(OperationRefusedException.class);
        todoService.complete(new User(new UniqueIdentifier<>("userId")), "someId");
    }

    @Test
    public void getCompleted_whenCompletedListFound_returnsCompletedListFromRepository() throws Exception {
        UniqueIdentifier uniqueIdentifier = new UniqueIdentifier<>("one@two.com");
        CompletedList completedListFromRepository = new CompletedList(uniqueIdentifier, Collections.emptyList());
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
        MasterList mockMasterList = mock(MasterList.class);
        when(mockMasterListRepository.find(any())).thenReturn(Optional.of(mockMasterList));
        List<Todo> todos = asList(
                new Todo("some task", ScheduledFor.later, 2),
                new Todo("some other task", ScheduledFor.later, 3));
        when(mockMasterList.move(any(), any())).thenReturn(todos);

        todoService.move(new User(new UniqueIdentifier<>("one@two.com")), "idOne", "idTwo");

        verify(mockMasterList).move("idOne", "idTwo");
        verify(mockMasterListRepository).update(mockMasterList, todos);
    }

    @Test
    public void move_whenMasterListFound_whenTodosFound_whenRepositoryRejectsModel_refusesOperation() throws Exception {
        MasterList mockMasterList = mock(MasterList.class);
        when(mockMasterListRepository.find(any())).thenReturn(Optional.of(mockMasterList));
        when(mockMasterList.move(any(), any())).thenReturn(Collections.emptyList());
        doThrow(new AbnormalModelException()).when(mockMasterListRepository).update(any(), anyListOf(Todo.class));

        exception.expect(OperationRefusedException.class);
        todoService.move(new User(new UniqueIdentifier<>("one@two.com")), "idOne", "idTwo");
    }

    @Test
    public void move_whenMasterListFound_whenTodosNotFound_refusesOperation() throws Exception {
        MasterList mockMasterList = mock(MasterList.class);
        when(mockMasterListRepository.find(any())).thenReturn(Optional.of(mockMasterList));
        when(mockMasterList.move(any(), any())).thenThrow(new TodoNotFoundException());

        exception.expect(OperationRefusedException.class);
        todoService.move(new User(new UniqueIdentifier<>("one@two.com")), "idOne", "idTwo");
    }

    @Test
    public void move_whenMasterListNotFound_refsusesOperation() throws Exception {
        when(mockMasterListRepository.find(any())).thenReturn(Optional.empty());

        exception.expect(OperationRefusedException.class);
        todoService.move(new User(new UniqueIdentifier<>("one@two.com")), "idOne", "idTwo");
    }

    @Test
    public void pull_whenMasterListFound_whenTodosPulled_updatesPulledTodosUsingRepository() throws Exception {
        MasterList mockMasterList = mock(MasterList.class);
        when(mockMasterListRepository.find(any())).thenReturn(Optional.of(mockMasterList));
        List<Todo> todos = asList(
                new Todo("some task", ScheduledFor.now, 1),
                new Todo("some other task", ScheduledFor.now, 2));
        when(mockMasterList.pull()).thenReturn(todos);

        todoService.pull(new User(new UniqueIdentifier<>("one@two.com")));

        verify(mockMasterList).pull();
        verify(mockMasterListRepository).update(mockMasterList, todos);
    }

    @Test
    public void pull_whenMasterListFound_whenTodosPulled_whenRepositoryRejectsModel_refusesOperation() throws Exception {
        MasterList mockMasterList = mock(MasterList.class);
        when(mockMasterListRepository.find(any())).thenReturn(Optional.of(mockMasterList));
        List<Todo> todos = asList(
                new Todo("some task", ScheduledFor.now, 1),
                new Todo("some other task", ScheduledFor.now, 2));
        when(mockMasterList.pull()).thenReturn(todos);
        doThrow(new AbnormalModelException()).when(mockMasterListRepository).update(any(), anyListOf(Todo.class));

        exception.expect(OperationRefusedException.class);
        todoService.pull(new User(new UniqueIdentifier<>("one@two.com")));
    }
}