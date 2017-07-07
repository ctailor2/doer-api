package com.doerapispring.api;

import com.doerapispring.authentication.AuthenticatedUser;
import com.doerapispring.domain.*;
import com.doerapispring.web.CompletedTodoListDTO;
import com.doerapispring.web.InvalidRequestException;
import com.doerapispring.web.TodoDTO;
import com.doerapispring.web.TodoListDTO;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.Date;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TodoApiServiceImplTest {
    private TodoApiServiceImpl todoApiServiceImpl;

    @Mock
    private TodoService mockTodoService;

    @Mock
    private ListService mockListService;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        todoApiServiceImpl = new TodoApiServiceImpl(mockTodoService);
    }

    @Test
    public void create_whenSchedulingCanBeParsed_callsTodoService() throws Exception {
        todoApiServiceImpl.create(new AuthenticatedUser("someIdentifier"), "someTask", "now");

        verify(mockTodoService).create(new User(new UniqueIdentifier<>("someIdentifier")), "someTask", ScheduledFor.now);
    }

    @Test
    public void create_whenSchedulingCannotBeParsed_throwsInvalidRequest() throws Exception {
        exception.expect(InvalidRequestException.class);
        todoApiServiceImpl.create(new AuthenticatedUser("someIdentifier"), "someTask", "bananas");

        verifyZeroInteractions(mockTodoService);
    }

    @Test
    public void create_whenTheOperationIsRefused_throwsInvalidRequest() throws Exception {
        doThrow(new OperationRefusedException()).when(mockTodoService).create(any(), any(), any());

        exception.expect(InvalidRequestException.class);
        todoApiServiceImpl.create(new AuthenticatedUser("someIdentifier"), "someTask", "now");
    }

    @Test
    public void delete_callsTodoService() throws Exception {
        todoApiServiceImpl.delete(new AuthenticatedUser("someIdentifier"), "someId");

        verify(mockTodoService).delete(new User(new UniqueIdentifier<>("someIdentifier")), "someId");
    }

    @Test
    public void delete_whenTheOperationIsRefused_throwsInvalidRequest() throws Exception {
        doThrow(new OperationRefusedException()).when(mockTodoService).delete(any(), any());

        exception.expect(InvalidRequestException.class);
        todoApiServiceImpl.delete(new AuthenticatedUser("someIdentifier"), "someId");
    }

    @Test
    public void displace_callsTodoService() throws Exception {
        todoApiServiceImpl.displace(new AuthenticatedUser("someIdentifier"), "someId", "someTask");

        verify(mockTodoService).displace(new User(new UniqueIdentifier<>("someIdentifier")), "someId", "someTask");
    }

    @Test
    public void displace_whenTheOperationIsRefused_throwsInvalidRequest() throws Exception {
        doThrow(new OperationRefusedException()).when(mockTodoService).displace(any(), any(), any());

        exception.expect(InvalidRequestException.class);
        todoApiServiceImpl.displace(new AuthenticatedUser("someIdentifier"), "someId", "someTask");
    }

    @Test
    public void update_callsTodoService() throws Exception {
        todoApiServiceImpl.update(new AuthenticatedUser("someIdentifier"), "someId", "someTask");

        verify(mockTodoService).update(new User(new UniqueIdentifier<>("someIdentifier")), "someId", "someTask");
    }

    @Test
    public void update_whenTheOperationIsRefused_throwsInvalidRequest() throws Exception {
        doThrow(new OperationRefusedException()).when(mockTodoService).update(any(), any(), any());

        exception.expect(InvalidRequestException.class);
        todoApiServiceImpl.update(new AuthenticatedUser("someIdentifier"), "someId", "someTask");
    }

    @Test
    public void complete_callsTodoService() throws Exception {
        todoApiServiceImpl.complete(new AuthenticatedUser("someIdentifier"), "someId");

        verify(mockTodoService).complete(new User(new UniqueIdentifier<>("someIdentifier")), "someId");
    }

    @Test
    public void complete_whenTheOperationIsRefused_throwsInvalidRequest() throws Exception {
        doThrow(new OperationRefusedException()).when(mockTodoService).complete(any(), any());

        exception.expect(InvalidRequestException.class);
        todoApiServiceImpl.complete(new AuthenticatedUser("someIdentifier"), "someId");
    }

    @Test
    public void getCompleted_callsTodoService_returnsCompletedTodoListDTO_containingAllTodos() throws Exception {
        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>("someIdentifier");
        Date completedAt = new Date();
        CompletedTodo completedTodo = new CompletedTodo("some task", completedAt);
        CompletedList completedList = new CompletedList(uniqueIdentifier, Collections.singletonList(completedTodo));
        when(mockTodoService.getCompleted(any())).thenReturn(completedList);

        CompletedTodoListDTO completedTodoListDTO = todoApiServiceImpl.getCompleted(new AuthenticatedUser("someIdentifier"));

        verify(mockTodoService).getCompleted(new User(uniqueIdentifier));
        assertThat(completedTodoListDTO.getTodoDTOs()).contains(
            new TodoDTO("some task", completedAt));
    }

    @Test
    public void getCompleted_whenTheOperationIsRefused_throwsInvalidRequest() throws Exception {
        when(mockTodoService.getCompleted(any())).thenThrow(new OperationRefusedException());

        exception.expect(InvalidRequestException.class);
        todoApiServiceImpl.getCompleted(new AuthenticatedUser("someIdentifier"));
    }

    @Test
    public void move_callsTodoService() throws Exception {
        todoApiServiceImpl.move(new AuthenticatedUser("someIdentifier"), "someId", "someOtherId");

        verify(mockTodoService).move(new User(new UniqueIdentifier<>("someIdentifier")), "someId", "someOtherId");
    }

    @Test
    public void move_whenOperationRefused_throwsInvalidRequest() throws Exception {
        doThrow(new OperationRefusedException()).when(mockTodoService).move(any(), any(), any());

        exception.expect(InvalidRequestException.class);
        todoApiServiceImpl.move(new AuthenticatedUser("someIdentifier"), "someId", "someOtherId");
    }

    @Test
    public void pull_callsTodoService() throws Exception {
        todoApiServiceImpl.pull(new AuthenticatedUser("someIdentifier"));

        verify(mockTodoService).pull(new User(new UniqueIdentifier<>("someIdentifier")));

    }

    @Test
    public void pull_whenOperationRefused_throwsInvalidRequest() throws Exception {
        doThrow(new OperationRefusedException()).when(mockTodoService).pull(any());

        exception.expect(InvalidRequestException.class);
        todoApiServiceImpl.pull(new AuthenticatedUser("someIdentifier"));
    }

    @Test
    public void getTodos_callsTodoService_forNowTodos() throws Exception {
        when(mockTodoService.getSubList(any(), any())).thenReturn(new TodoList(ScheduledFor.now, Collections.emptyList(), 1));
        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>("someIdentifier");

        todoApiServiceImpl.getTodos(new AuthenticatedUser("someIdentifier"));

        verify(mockTodoService).getSubList(new User(uniqueIdentifier), ScheduledFor.now);
    }

    @Test
    public void getTodos_whenListIsFull_returnsTodoList_whereDisplacementIsAllowed() throws Exception {
        when(mockTodoService.getSubList(any(), any()))
            .thenReturn(new TodoList(ScheduledFor.now, Collections.singletonList(new Todo("someTask", ScheduledFor.now, 1)), 1));

        TodoListDTO todoListDTO = todoApiServiceImpl.getTodos(new AuthenticatedUser("someIdentifier"));

        assertThat(todoListDTO.isFull()).isEqualTo(true);
    }

    @Test
    public void getTodos_whenListIsNotFull_returnsTodoList_whereDisplacementIsNotAllowed() throws Exception {
        when(mockTodoService.getSubList(any(), any()))
            .thenReturn(new TodoList(ScheduledFor.later, Collections.emptyList(), -1));

        TodoListDTO todoListDTO = todoApiServiceImpl.getTodos(new AuthenticatedUser("someIdentifier"));

        assertThat(todoListDTO.isFull()).isEqualTo(false);
    }

    @Test
    public void getDeferredTodos_callsTodoService_forLaterTodos() throws Exception {
        when(mockTodoService.getSubList(any(), any())).thenReturn(new TodoList(ScheduledFor.now, Collections.emptyList(), 1));
        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>("someIdentifier");

        todoApiServiceImpl.getDeferredTodos(new AuthenticatedUser("someIdentifier"));

        verify(mockTodoService).getSubList(new User(uniqueIdentifier), ScheduledFor.later);
    }

    @Test
    public void getDeferredTodos_whenListIsFull_returnsTodoList_whereDisplacementIsAllowed() throws Exception {
        when(mockTodoService.getSubList(any(), any()))
            .thenReturn(new TodoList(ScheduledFor.now, Collections.singletonList(new Todo("someTask", ScheduledFor.now, 1)), 1));

        TodoListDTO todoListDTO = todoApiServiceImpl.getDeferredTodos(new AuthenticatedUser("someIdentifier"));

        assertThat(todoListDTO.isFull()).isEqualTo(true);
    }

    @Test
    public void getDeferredTodos_whenListIsNotFull_returnsTodoList_whereDisplacementIsNotAllowed() throws Exception {
        when(mockTodoService.getSubList(any(), any()))
            .thenReturn(new TodoList(ScheduledFor.later, Collections.emptyList(), -1));

        TodoListDTO todoListDTO = todoApiServiceImpl.getDeferredTodos(new AuthenticatedUser("someIdentifier"));

        assertThat(todoListDTO.isFull()).isEqualTo(false);
    }
}