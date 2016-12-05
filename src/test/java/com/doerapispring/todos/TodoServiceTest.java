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

import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Created by chiragtailor on 9/28/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class TodoServiceTest {
    private TodoService todoService;

    @Mock
    private NewTodoRepository newTodoRepository;

    @Mock
    private TodoDao todoDao;

    @Captor
    private ArgumentCaptor<TodoEntity> todoArgumentCaptor;

    @Captor
    private ArgumentCaptor<NewTodo> newTodoArgumentCaptor;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        todoService = new TodoService(newTodoRepository, todoDao);
    }

    @Test
    public void get_withNoType_callsTodoRepository_returnsTodos() throws Exception {
        List<TodoEntity> todos = Arrays.asList(TodoEntity.builder()
                .task("clean the fridge")
                .active(true)
                .build());
        doReturn(todos).when(todoDao).findByUserEmail("one@two.com");

        List<Todo> todoEntities = todoService.get("one@two.com", null);

        verify(todoDao).findByUserEmail("one@two.com");
        Todo todo = todoEntities.get(0);
        assertThat(todo).isNotNull();
        assertThat(todo.getTask()).isEqualTo("clean the fridge");
        assertThat(todo.isActive()).isEqualTo(true);
    }

    @Test
    public void get_withTypeActive_callsTodoRepository_forActiveTodos_returnsTodos() throws Exception {
        List<TodoEntity> todos = Arrays.asList(TodoEntity.builder()
                .task("clean the fridge")
                .active(true)
                .build());

        doReturn(todos).when(todoDao).findByUserEmailAndType("one@two.com", true);

        List<Todo> todoEntities = todoService.get("one@two.com", TodoTypeParamEnum.active);

        verify(todoDao).findByUserEmailAndType("one@two.com", true);
        verifyNoMoreInteractions(todoDao);
        Todo todo = todoEntities.get(0);
        assertThat(todo).isNotNull();
        assertThat(todo.getTask()).isEqualTo("clean the fridge");
        assertThat(todo.isActive()).isEqualTo(true);
    }

    @Test
    public void get_withTypeInactive_callsTodoRepository_forInactiveTodos_returnsTodos() throws Exception {
        List<TodoEntity> todos = Arrays.asList(TodoEntity.builder()
                .task("clean the fridge")
                .active(true)
                .build());

        doReturn(todos).when(todoDao).findByUserEmailAndType("one@two.com", false);

        List<Todo> todoEntities = todoService.get("one@two.com", TodoTypeParamEnum.inactive);

        verify(todoDao).findByUserEmailAndType("one@two.com", false);
        verifyNoMoreInteractions(todoDao);
        Todo todo = todoEntities.get(0);
        assertThat(todo).isNotNull();
        assertThat(todo.getTask()).isEqualTo("clean the fridge");
        assertThat(todo.isActive()).isEqualTo(true);
    }

    @Test
    public void get_withNullType_freaksOutMaybe() throws Exception {
        todoService.get("one@two.com", null);
    }

    @Test
    public void newCreate_addsTodoToRepository() throws Exception {
        UserIdentifier userIdentifier = new UserIdentifier("testItUp");
        String task = "some things";
        ScheduledFor scheduling = ScheduledFor.now;
        todoService.newCreate(userIdentifier, task, scheduling);

        verify(newTodoRepository).add(newTodoArgumentCaptor.capture());
        NewTodo todo = newTodoArgumentCaptor.getValue();
        assertThat(todo).isNotNull();
        assertThat(todo.getUserIdentifier()).isEqualTo(userIdentifier);
        assertThat(todo.getTask()).isEqualTo(task);
        assertThat(todo.getScheduling()).isEqualTo(scheduling);
    }

    @Test
    public void newCreate_whenRepositoryRejectsModel_refusesCreate() throws Exception {
        doThrow(new AbnormalModelException()).when(newTodoRepository).add(any());

        exception.expect(OperationRefusedException.class);
        todoService.newCreate(new UserIdentifier("testItUp"), "some things", ScheduledFor.now);
    }
}