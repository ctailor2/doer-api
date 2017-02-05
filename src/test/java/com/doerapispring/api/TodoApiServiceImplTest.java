package com.doerapispring.api;

import com.doerapispring.authentication.AuthenticatedUser;
import com.doerapispring.domain.*;
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

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TodoApiServiceImplTest {
    private TodoApiServiceImpl todoApiServiceImpl;

    @Mock
    private TodoService mockTodoService;

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
    public void get_callsTodoService_returnsMasterListDTO_containingAllTodos() throws Exception {
        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>("someIdentifier");
        MasterList masterList = new MasterList(uniqueIdentifier, 400);
        Todo first = masterList.add("first", ScheduledFor.now);
        Todo second = masterList.add("second", ScheduledFor.later);
        when(mockTodoService.get(any())).thenReturn(masterList);

        TodoListDTO todoListDTO = todoApiServiceImpl.get(new AuthenticatedUser("someIdentifier"));

        verify(mockTodoService).get(new User(uniqueIdentifier));
        assertThat(todoListDTO.getTodoDTOs()).contains(
                new TodoDTO(first.getLocalIdentifier(), "first", "now"),
                new TodoDTO(second.getLocalIdentifier(), "second", "later"));
    }

    @Test
    public void get_callsTodoService_whenImmediateListFull_returnsTodoList_whereCanNotScheduleForNow() throws Exception {
        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>("someIdentifier");
        when(mockTodoService.get(any())).thenReturn(new MasterList(uniqueIdentifier, 0
        ));

        TodoListDTO todoListDTO = todoApiServiceImpl.get(new AuthenticatedUser("someIdentifier"));

        verify(mockTodoService).get(new User(uniqueIdentifier));
        assertThat(todoListDTO).isEqualTo(new TodoListDTO(Collections.emptyList(), false));
    }

    @Test
    public void get_callsTodoService_whenImmediateListNotFull_returnsMasterListDTO_whereCanScheduleForNow() throws Exception {
        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>("someIdentifier");
        when(mockTodoService.get(any())).thenReturn(new MasterList(uniqueIdentifier, 1
        ));

        TodoListDTO todoListDTO = todoApiServiceImpl.get(new AuthenticatedUser("someIdentifier"));

        verify(mockTodoService).get(new User(uniqueIdentifier));
        assertThat(todoListDTO).isEqualTo(new TodoListDTO(Collections.emptyList(), true));
    }

    @Test
    public void get_whenTheOperationIsRefused_throwsInvalidRequest() throws Exception {
        when(mockTodoService.get(any())).thenThrow(new OperationRefusedException());

        exception.expect(InvalidRequestException.class);
        todoApiServiceImpl.get(new AuthenticatedUser("someIdentifier"));
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
}