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
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ListServiceTest {
    private ListService listService;

    @Mock
    private OwnedObjectRepository<TodoList, UserId, ListId> mockTodoListRepository;

    @Mock
    private OwnedObjectRepository<CompletedTodo, UserId, CompletedTodoId> mockCompletedTodoRepository;

    @Rule
    public ExpectedException exception = ExpectedException.none();
    private TodoList todoList;
    private String identifier;

    @Before
    public void setUp() throws Exception {
        listService = new ListService(mockTodoListRepository, mockCompletedTodoRepository);
        identifier = "userId";
        todoList = mock(TodoList.class);
        when(mockTodoListRepository.findFirst(any())).thenReturn(Optional.of(todoList));
        when(mockTodoListRepository.find(any(), any())).thenReturn(Optional.of(todoList));
    }

    @Test
    public void unlock_whenTodoListFound_unlocksTodoList_andSavesIt() throws Exception {
        listService.unlock(new User(new UserId(identifier)), new ListId("someListId"));

        verify(todoList).unlock();
        verify(mockTodoListRepository).save(todoList);
    }

    @Test
    public void unlock_whenTodoListFound_whenRepositoryRejectsModels_refusesOperation() throws Exception {
        doThrow(new AbnormalModelException()).when(mockTodoListRepository).save(any());

        exception.expect(InvalidRequestException.class);
        listService.unlock(new User(new UserId(identifier)), new ListId("someListId"));
    }

    @Test
    public void unlock_whenTodoListFound_whenLockTimerNotExpired_refusesOperation() throws Exception {
        doThrow(new LockTimerNotExpiredException()).when(todoList).unlock();

        exception.expect(InvalidRequestException.class);
        listService.unlock(new User(new UserId(identifier)), new ListId("someListId"));
    }

    @Test
    public void unlock_whenTodoListNotFound_refusesOperation() throws Exception {
        when(mockTodoListRepository.find(any(), any())).thenReturn(Optional.empty());

        exception.expect(InvalidRequestException.class);
        listService.unlock(new User(new UserId(identifier)), new ListId("someListId"));
    }

    @Test
    public void getDefault_whenTodoListFound_returnsTodoListFromRepository() throws Exception {
        TodoList mockTodoList = mock(TodoList.class);
        when(mockTodoListRepository.findFirst(any())).thenReturn(Optional.of(mockTodoList));
        ReadOnlyTodoList mockReadOnlyTodoList = mock(ReadOnlyTodoList.class);
        when(mockTodoList.read()).thenReturn(mockReadOnlyTodoList);
        User user = new User(new UserId(identifier));

        ReadOnlyTodoList actual = listService.getDefault(user);

        assertThat(actual).isEqualTo(mockReadOnlyTodoList);
    }

    @Test
    public void getDefault_whenTodoListNotFound_refusesOperation() throws Exception {
        when(mockTodoListRepository.findFirst(any())).thenReturn(Optional.empty());

        exception.expect(InvalidRequestException.class);
        listService.getDefault(new User(new UserId(identifier)));
    }

    @Test
    public void get_whenTodoListFound_returnsTodoListFromRepository() throws Exception {
        TodoList mockTodoList = mock(TodoList.class);
        when(mockTodoListRepository.find(any(), any())).thenReturn(Optional.of(mockTodoList));
        ReadOnlyTodoList mockReadOnlyTodoList = mock(ReadOnlyTodoList.class);
        when(mockTodoList.read()).thenReturn(mockReadOnlyTodoList);
        User user = new User(new UserId(identifier));

        ReadOnlyTodoList actual = listService.get(user, new ListId("someListId"));

        assertThat(actual).isEqualTo(mockReadOnlyTodoList);
    }

    @Test
    public void get_whenTodoListNotFound_refusesOperation() throws Exception {
        when(mockTodoListRepository.find(any(), any())).thenReturn(Optional.empty());

        exception.expect(InvalidRequestException.class);
        listService.get(new User(new UserId(identifier)), new ListId("someListId"));
    }

    @Test
    public void get_whenCompletedListFound_returnsCompletedListFromRepository() throws Exception {
        List<CompletedTodo> expectedTodos = singletonList(new CompletedTodo(
            new UserId("someUserId"),
            new ListId("someListId"),
            new CompletedTodoId("someTodoId"),
            "someTask",
            Date.from(Instant.now())));
        when(mockCompletedTodoRepository.findAll(any(UserId.class))).thenReturn(expectedTodos);

        List<CompletedTodo> actualTodos = listService.getCompleted(new User(new UserId(identifier)));

        assertThat(actualTodos).isEqualTo(expectedTodos);
    }
}