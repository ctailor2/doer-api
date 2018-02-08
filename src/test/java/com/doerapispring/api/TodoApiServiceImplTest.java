package com.doerapispring.api;

import com.doerapispring.authentication.AuthenticatedUser;
import com.doerapispring.domain.*;
import com.doerapispring.web.CompletedTodoListDTO;
import com.doerapispring.web.InvalidRequestException;
import com.doerapispring.web.TodoDTO;
import com.doerapispring.web.TodoListDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TodoApiServiceImplTest {
    private TodoApiServiceImpl todoApiServiceImpl;

    @Mock
    private TodoService mockTodoService;
    private UniqueIdentifier<String> uniqueIdentifier;
    private MasterList masterList;

    @Before
    public void setUp() throws Exception {
        todoApiServiceImpl = new TodoApiServiceImpl(mockTodoService);
        uniqueIdentifier = new UniqueIdentifier<>("someIdentifier");
        masterList = new MasterList(Clock.systemDefaultZone(), uniqueIdentifier, new ArrayList<>());
        when(mockTodoService.get(any())).thenReturn(masterList);
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
        todoApiServiceImpl.displace(new AuthenticatedUser("someIdentifier"), "someId", "someTask");

        verify(mockTodoService).displace(new User(new UniqueIdentifier<>("someIdentifier")), "someId", "someTask");
    }

    @Test
    public void displace_whenTheOperationIsRefused_throwsInvalidRequest() throws Exception {
        String exceptionMessage = "some exception message";
        doThrow(new OperationRefusedException(exceptionMessage)).when(mockTodoService).displace(any(), any(), any());

        assertThatThrownBy(() ->
            todoApiServiceImpl.displace(new AuthenticatedUser("someIdentifier"), "someId", "someTask"))
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

    @Test
    public void getTodos_callsTodoService() throws Exception {

        todoApiServiceImpl.getTodos(new AuthenticatedUser("someIdentifier"));

        verify(mockTodoService).get(new User(uniqueIdentifier));
    }

    @Test
    public void getTodos_callsTodoService_returnsTodosFromImmediateList() throws Exception {
        Todo todo = masterList.add("someTask");
        masterList.addDeferred("someOtherTask");

        TodoListDTO todoListDTO = todoApiServiceImpl.getTodos(new AuthenticatedUser("someIdentifier"));

        assertThat(todoListDTO.getTodoDTOs()).containsOnly(new TodoDTO(todo.getLocalIdentifier(), todo.getTask()));
    }

    @Test
    public void getTodos_returnsTodoList_withFullIndicator() throws Exception {
        masterList.add("someTask");
        masterList.add("someOtherTask");

        TodoListDTO todoListDTO = todoApiServiceImpl.getTodos(new AuthenticatedUser("someIdentifier"));

        assertThat(todoListDTO.isFull()).isEqualTo(true);
    }

    @Test
    public void getDeferredTodos_callsTodoService() throws Exception {
        todoApiServiceImpl.getDeferredTodos(new AuthenticatedUser("someIdentifier"));

        verify(mockTodoService).getDeferredTodos(new User(uniqueIdentifier));
    }

    @Test
    public void getDeferredTodos_callsTodoService_returnsDeferredTodos() throws Exception {
        Todo todo = new Todo("someIdentifier", "someTask", MasterList.NAME, 1);
        when(mockTodoService.getDeferredTodos(any())).thenReturn(Collections.singletonList(todo));

        TodoListDTO todoListDTO = todoApiServiceImpl.getDeferredTodos(new AuthenticatedUser("someIdentifier"));

        assertThat(todoListDTO).isNotNull();
        assertThat(todoListDTO.getTodoDTOs()).containsOnly(new TodoDTO("someIdentifier", "someTask"));
    }

    @Test
    public void getDeferredTodos_returnsTodoList_withFullIndicator_setToFalse() throws Exception {
        TodoListDTO todoListDTO = todoApiServiceImpl.getDeferredTodos(new AuthenticatedUser("someIdentifier"));

        assertThat(todoListDTO.isFull()).isEqualTo(false);
    }
}