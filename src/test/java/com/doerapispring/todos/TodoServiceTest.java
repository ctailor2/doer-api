package com.doerapispring.todos;

import com.doerapispring.AbnormalModelException;
import com.doerapispring.UserIdentifier;
import com.doerapispring.userSessions.OperationRefusedException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Created by chiragtailor on 9/28/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class TodoServiceTest {
    private TodoService todoService;

    @Mock
    private TodoRepository todoRepository;

    @Captor
    private ArgumentCaptor<Todo> todoArgumentCaptor;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        todoService = new TodoService(todoRepository);
    }

    @Test
    public void getByScheduling_usesRepository() throws Exception {
        UserIdentifier userIdentifier = new UserIdentifier("one@two.com");
        ScheduledFor scheduling = ScheduledFor.now;
        todoService.getByScheduling(userIdentifier, scheduling);

        verify(todoRepository).findByScheduling(userIdentifier, scheduling);
    }

    @Test
    public void create_addsTodoToRepository() throws Exception {
        UserIdentifier userIdentifier = new UserIdentifier("testItUp");
        String task = "some things";
        ScheduledFor scheduling = ScheduledFor.now;
        todoService.create(userIdentifier, task, scheduling);

        verify(todoRepository).add(todoArgumentCaptor.capture());
        Todo todo = todoArgumentCaptor.getValue();
        assertThat(todo).isNotNull();
        assertThat(todo.getUserIdentifier()).isEqualTo(userIdentifier);
        assertThat(todo.getTask()).isEqualTo(task);
        assertThat(todo.getScheduling()).isEqualTo(scheduling);
    }

    @Test
    public void create_whenRepositoryRejectsModel_refusesCreate() throws Exception {
        doThrow(new AbnormalModelException()).when(todoRepository).add(any());

        exception.expect(OperationRefusedException.class);
        todoService.create(new UserIdentifier("testItUp"), "some things", ScheduledFor.now);
    }
}