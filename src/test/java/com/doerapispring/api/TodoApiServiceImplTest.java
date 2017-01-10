package com.doerapispring.api;

import com.doerapispring.authentication.AuthenticatedUser;
import com.doerapispring.domain.*;
import com.doerapispring.web.InvalidRequestException;
import com.doerapispring.web.TodoDTO;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static java.util.Arrays.asList;
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
    public void create_whenSchedulingCanBeParsed_callsTodoService_returnsTodoDTO() throws Exception {
        String task = "something";
        when(mockTodoService.create(any(), any(), any())).thenReturn(new Todo(task, ScheduledFor.later));

        TodoDTO todoDTO = todoApiServiceImpl.create(new AuthenticatedUser("someIdentifier"), "someTask", "now");

        verify(mockTodoService).create(new User(new UniqueIdentifier("someIdentifier")), "someTask", ScheduledFor.now);
        assertThat(todoDTO.getTask()).isEqualTo(task);
        assertThat(todoDTO.getScheduling()).isEqualTo("later");
    }

    @Test
    public void create_whenSchedulingCannotBeParsed_throwsInvalidRequest() throws Exception {
        exception.expect(InvalidRequestException.class);
        todoApiServiceImpl.create(new AuthenticatedUser("someIdentifier"), "someTask", "bananas");

        verifyZeroInteractions(mockTodoService);
    }

    @Test
    public void create_whenTheOperationIsRefused_throwsInvalidRequest() throws Exception {
        when(mockTodoService.create(any(), any(), any())).thenThrow(new OperationRefusedException());

        exception.expect(InvalidRequestException.class);
        todoApiServiceImpl.create(new AuthenticatedUser("someIdentifier"), "someTask", "bananas");

        verifyZeroInteractions(mockTodoService);
    }

    @Test
    public void getByScheduling_whenSchedulingCanBeParsed_callsTodoService_returnsTodoDTOs() throws Exception {
        when(mockTodoService.getByScheduling(any(), any())).thenReturn(asList(
                new Todo("first", ScheduledFor.now),
                new Todo("second", ScheduledFor.later)));

        List<TodoDTO> todoDTOs = todoApiServiceImpl.getByScheduling(new AuthenticatedUser("someIdentifier"), "now");

        verify(mockTodoService).getByScheduling(new User(new UniqueIdentifier("someIdentifier")), ScheduledFor.now);
        assertThat(todoDTOs).contains(new TodoDTO("first", "now"), new TodoDTO("second", "later"));
    }

    @Test
    public void getByScheduling_whenSchedulingCannotBeParsed_throwsInvalidRequest() throws Exception {
        exception.expect(InvalidRequestException.class);
        todoApiServiceImpl.getByScheduling(new AuthenticatedUser("someIdentifier"), "bananas");

        verifyZeroInteractions(mockTodoService);
    }
}