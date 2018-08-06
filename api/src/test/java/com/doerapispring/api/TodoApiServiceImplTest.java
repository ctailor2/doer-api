package com.doerapispring.api;

import com.doerapispring.authentication.AuthenticatedUser;
import com.doerapispring.domain.*;
import com.doerapispring.web.CompletedTodoDTO;
import com.doerapispring.web.CompletedTodoListDTO;
import com.doerapispring.web.InvalidRequestException;
import org.junit.Before;
import org.junit.Test;

import java.time.Clock;
import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class TodoApiServiceImplTest {
    private TodoApiServiceImpl todoApiServiceImpl;

    private TodoService mockTodoService;

    @Before
    public void setUp() throws Exception {
        mockTodoService = mock(TodoService.class);
        todoApiServiceImpl = new TodoApiServiceImpl(mockTodoService);
    }

    @Test
    public void create_callsTodoService() throws Exception {
        todoApiServiceImpl.create(new AuthenticatedUser("someIdentifier"), "someTask");

        verify(mockTodoService).create(new User(new UniqueIdentifier<>("someIdentifier")), "someTask");
    }

    @Test
    public void create_whenTheOperationIsRefused_throwsInvalidRequest() throws Exception {
        String exceptionMessage = "some exception message";
        doThrow(new OperationRefusedException(exceptionMessage)).when(mockTodoService).create(any(), any());

        assertThatThrownBy(() ->
            todoApiServiceImpl.create(new AuthenticatedUser("someIdentifier"), "someTask"))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessage(exceptionMessage);
    }

    @Test
    public void createDeferred_callsTodoService() throws Exception {
        todoApiServiceImpl.createDeferred(new AuthenticatedUser("someIdentifier"), "someTask");

        verify(mockTodoService).createDeferred(new User(new UniqueIdentifier<>("someIdentifier")), "someTask");
    }

    @Test
    public void createDeferred_whenTheOperationIsRefused_throwsInvalidRequest() throws Exception {
        String exceptionMessage = "some exception message";
        doThrow(new OperationRefusedException(exceptionMessage)).when(mockTodoService).createDeferred(any(), any());

        assertThatThrownBy(() ->
            todoApiServiceImpl.createDeferred(new AuthenticatedUser("someIdentifier"), "someTask"))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessage(exceptionMessage);
    }

    @Test
    public void delete_callsTodoService() throws Exception {
        todoApiServiceImpl.delete(new AuthenticatedUser("someIdentifier"), "someId");

        verify(mockTodoService).delete(new User(new UniqueIdentifier<>("someIdentifier")), "someId");
    }

    @Test
    public void delete_whenTheOperationIsRefused_throwsInvalidRequest() throws Exception {
        doThrow(new OperationRefusedException()).when(mockTodoService).delete(any(), any());

        assertThatThrownBy(() ->
            todoApiServiceImpl.delete(new AuthenticatedUser("someIdentifier"), "someId"))
            .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    public void displace_callsTodoService() throws Exception {
        todoApiServiceImpl.displace(new AuthenticatedUser("someIdentifier"), "someTask");

        verify(mockTodoService).displace(new User(new UniqueIdentifier<>("someIdentifier")), "someTask");
    }

    @Test
    public void displace_whenTheOperationIsRefused_throwsInvalidRequest() throws Exception {
        String exceptionMessage = "some exception message";
        doThrow(new OperationRefusedException(exceptionMessage)).when(mockTodoService).displace(any(), any());

        assertThatThrownBy(() ->
            todoApiServiceImpl.displace(new AuthenticatedUser("someIdentifier"), "someTask"))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessage(exceptionMessage);
    }

    @Test
    public void update_callsTodoService() throws Exception {
        todoApiServiceImpl.update(new AuthenticatedUser("someIdentifier"), "someId", "someTask");

        verify(mockTodoService).update(new User(new UniqueIdentifier<>("someIdentifier")), "someId", "someTask");
    }

    @Test
    public void update_whenTheOperationIsRefused_throwsInvalidRequest() throws Exception {
        String exceptionMessage = "some exception message";
        doThrow(new OperationRefusedException(exceptionMessage)).when(mockTodoService).update(any(), any(), any());

        assertThatThrownBy(() ->
            todoApiServiceImpl.update(new AuthenticatedUser("someIdentifier"), "someId", "someTask"))
            .isInstanceOf(InvalidRequestException.class)
            .hasMessage(exceptionMessage);
    }

    @Test
    public void complete_callsTodoService() throws Exception {
        todoApiServiceImpl.complete(new AuthenticatedUser("someIdentifier"), "someId");

        verify(mockTodoService).complete(new User(new UniqueIdentifier<>("someIdentifier")), "someId");
    }

    @Test
    public void complete_whenTheOperationIsRefused_throwsInvalidRequest() throws Exception {
        doThrow(new OperationRefusedException()).when(mockTodoService).complete(any(), any());

        assertThatThrownBy(() ->
            todoApiServiceImpl.complete(new AuthenticatedUser("someIdentifier"), "someId"))
            .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    public void getCompleted_callsTodoService_returnsCompletedTodoListDTO_containingAllTodos() throws Exception {
        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>("someIdentifier");
        Date completedAt = new Date();
        CompletedTodo completedTodo = new CompletedTodo("someUuid", "some task", completedAt);
        CompletedList completedList = new CompletedList(mock(Clock.class), uniqueIdentifier, Collections.singletonList(completedTodo));
        when(mockTodoService.getCompleted(any())).thenReturn(completedList);

        CompletedTodoListDTO completedTodoListDTO = todoApiServiceImpl.getCompleted(new AuthenticatedUser("someIdentifier"));

        verify(mockTodoService).getCompleted(new User(uniqueIdentifier));
        assertThat(completedTodoListDTO.getCompletedTodoDTOs()).contains(
            new CompletedTodoDTO("some task", completedAt));
    }

    @Test
    public void getCompleted_whenTheOperationIsRefused_throwsInvalidRequest() throws Exception {
        when(mockTodoService.getCompleted(any())).thenThrow(new OperationRefusedException());

        assertThatThrownBy(() ->
            todoApiServiceImpl.getCompleted(new AuthenticatedUser("someIdentifier")))
            .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    public void move_callsTodoService() throws Exception {
        todoApiServiceImpl.move(new AuthenticatedUser("someIdentifier"), "someId", "someOtherId");

        verify(mockTodoService).move(new User(new UniqueIdentifier<>("someIdentifier")), "someId", "someOtherId");
    }

    @Test
    public void move_whenOperationRefused_throwsInvalidRequest() throws Exception {
        doThrow(new OperationRefusedException()).when(mockTodoService).move(any(), any(), any());

        assertThatThrownBy(() ->
            todoApiServiceImpl.move(new AuthenticatedUser("someIdentifier"), "someId", "someOtherId"))
            .isInstanceOf(InvalidRequestException.class);
    }

    @Test
    public void pull_callsTodoService() throws Exception {
        todoApiServiceImpl.pull(new AuthenticatedUser("someIdentifier"));

        verify(mockTodoService).pull(new User(new UniqueIdentifier<>("someIdentifier")));

    }

    @Test
    public void pull_whenOperationRefused_throwsInvalidRequest() throws Exception {
        doThrow(new OperationRefusedException()).when(mockTodoService).pull(any());

        assertThatThrownBy(() ->
            todoApiServiceImpl.pull(new AuthenticatedUser("someIdentifier")))
            .isInstanceOf(InvalidRequestException.class);
    }
}