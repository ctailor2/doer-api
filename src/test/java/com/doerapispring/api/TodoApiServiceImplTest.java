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

import java.time.Clock;
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
    public void create_callsTodoService() throws Exception {
        todoApiServiceImpl.create(new AuthenticatedUser("someIdentifier"), "someTask");

        verify(mockTodoService).create(new User(new UniqueIdentifier<>("someIdentifier")), "someTask");
    }

    @Test
    public void create_whenTheOperationIsRefused_throwsInvalidRequest() throws Exception {
        doThrow(new OperationRefusedException()).when(mockTodoService).create(any(), any());

        exception.expect(InvalidRequestException.class);
        todoApiServiceImpl.create(new AuthenticatedUser("someIdentifier"), "someTask");
    }

    @Test
    public void createDeferred_callsTodoService() throws Exception {
        todoApiServiceImpl.createDeferred(new AuthenticatedUser("someIdentifier"), "someTask");

        verify(mockTodoService).createDeferred(new User(new UniqueIdentifier<>("someIdentifier")), "someTask");
    }

    @Test
    public void createDeferred_whenTheOperationIsRefused_throwsInvalidRequest() throws Exception {
        doThrow(new OperationRefusedException()).when(mockTodoService).createDeferred(any(), any());

        exception.expect(InvalidRequestException.class);
        todoApiServiceImpl.createDeferred(new AuthenticatedUser("someIdentifier"), "someTask");
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
    public void getTodos_callsTodoService() throws Exception {
        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>("someIdentifier");
        when(mockTodoService.get(any())).thenReturn(mock(MasterList.class));

        todoApiServiceImpl.getTodos(new AuthenticatedUser("someIdentifier"));

        verify(mockTodoService).get(new User(uniqueIdentifier));
    }

    @Test
    public void getTodos_callsTodoService_returnsTodosFromImmediateList() throws Exception {
        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>("someIdentifier");
        Todo nowTodo = new Todo("someIdentifier", "someTask", MasterList.NAME, 1);
        Todo laterTodo = new Todo("someIdentifier", "someOtherTask", MasterList.DEFERRED_NAME, 1);
        MasterList masterList = new MasterList(
            Clock.systemDefaultZone(),
            uniqueIdentifier,
            new TodoList(ScheduledFor.now, Collections.singletonList(nowTodo), 2),
            new TodoList(ScheduledFor.later, Collections.singletonList(laterTodo), -1), Collections.emptyList());
        when(mockTodoService.get(any())).thenReturn(masterList);

        TodoListDTO todoListDTO = todoApiServiceImpl.getTodos(new AuthenticatedUser("someIdentifier"));
        assertThat(todoListDTO.getTodoDTOs()).containsOnly(new TodoDTO("someIdentifier", "someTask"));
    }

    @Test
    public void getTodos_returnsTodoList_withFullIndicator() throws Exception {
        MasterList mockMasterList = mock(MasterList.class);
        when(mockTodoService.get(any())).thenReturn(mockMasterList);
        when(mockMasterList.isFull()).thenReturn(true);

        TodoListDTO todoListDTO = todoApiServiceImpl.getTodos(new AuthenticatedUser("someIdentifier"));

        assertThat(todoListDTO.isFull()).isEqualTo(true);
    }

    @Test
    public void getDeferredTodos_callsTodoService() throws Exception {
        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>("someIdentifier");
        when(mockTodoService.get(any())).thenReturn(mock(MasterList.class));

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