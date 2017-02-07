package com.doerapispring.domain;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        todoService = new TodoService(mockMasterListRepository);
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

        verify(mockMasterList).delete("someTodoId");
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
        doThrow(new AbnormalModelException()).when(mockMasterListRepository).update(any(), any());

        exception.expect(OperationRefusedException.class);
        todoService.displace(new User(new UniqueIdentifier<>("userId")), "someId", "someTask");
    }
}