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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ListServiceTest {
    private ListService listService;

    @Mock
    private ObjectRepository<TodoList, String> mockTodoListRepository;

    @Mock
    private ObjectRepository<CompletedList, String> mockCompletedListRepository;

    @Rule
    public ExpectedException exception = ExpectedException.none();
    private TodoList todoList;
    private UniqueIdentifier<String> uniqueIdentifier;

    @Before
    public void setUp() throws Exception {
        listService = new ListService(mockTodoListRepository, mockCompletedListRepository);
        uniqueIdentifier = new UniqueIdentifier<>("userId");
        todoList = mock(TodoList.class);
        when(mockTodoListRepository.find(any())).thenReturn(Optional.of(todoList));
    }

    @Test
    public void unlock_whenTodoListFound_unlocksTodoList_andSavesIt() throws Exception {
        listService.unlock(new User(uniqueIdentifier));

        verify(todoList).unlock();
        verify(mockTodoListRepository).save(todoList);
    }

    @Test
    public void unlock_whenTodoListFound_whenRepositoryRejectsModels_refusesOperation() throws Exception {
        doThrow(new AbnormalModelException()).when(mockTodoListRepository).save(any());

        exception.expect(InvalidRequestException.class);
        listService.unlock(new User(uniqueIdentifier));
    }

    @Test
    public void unlock_whenTodoListFound_whenLockTimerNotExpired_refusesOperation() throws Exception {
        doThrow(new LockTimerNotExpiredException()).when(todoList).unlock();

        exception.expect(InvalidRequestException.class);
        listService.unlock(new User(uniqueIdentifier));
    }

    @Test
    public void unlock_whenTodoListNotFound_refusesOperation() throws Exception {
        when(mockTodoListRepository.find(any())).thenReturn(Optional.empty());

        exception.expect(InvalidRequestException.class);
        listService.unlock(new User(uniqueIdentifier));
    }

    @Test
    public void get_whenTodoListFound_returnsTodoListFromRepository() throws Exception {
        TodoList mockTodoList = mock(TodoList.class);
        when(mockTodoListRepository.find(any())).thenReturn(Optional.of(mockTodoList));
        ReadOnlyTodoList mockReadOnlyTodoList = mock(ReadOnlyTodoList.class);
        when(mockTodoList.read()).thenReturn(mockReadOnlyTodoList);
        User user = new User(uniqueIdentifier);

        ReadOnlyTodoList actual = listService.get(user);

        assertThat(actual).isEqualTo(mockReadOnlyTodoList);
    }

    @Test
    public void get_whenTodoListNotFound_refusesOperation() throws Exception {
        when(mockTodoListRepository.find(any())).thenReturn(Optional.empty());

        exception.expect(InvalidRequestException.class);
        listService.get(new User(uniqueIdentifier));
    }

    @Test
    public void get_whenCompletedListFound_returnsCompletedListFromRepository() throws Exception {
        CompletedList mockCompletedList = mock(CompletedList.class);
        when(mockCompletedListRepository.find(any())).thenReturn(Optional.of(mockCompletedList));
        ReadOnlyCompletedList mockReadOnlyCompletedList = mock(ReadOnlyCompletedList.class);
        when(mockCompletedList.read()).thenReturn(mockReadOnlyCompletedList);
        User user = new User(uniqueIdentifier);

        ReadOnlyCompletedList readOnlyCompletedList = listService.getCompleted(user);

        assertThat(readOnlyCompletedList).isEqualTo(mockReadOnlyCompletedList);
    }

    @Test
    public void get_whenCompletedListNotFound_refusesOperation() throws Exception {
        when(mockCompletedListRepository.find(any())).thenReturn(Optional.empty());

        exception.expect(InvalidRequestException.class);
        listService.getCompleted(new User(uniqueIdentifier));
    }
}