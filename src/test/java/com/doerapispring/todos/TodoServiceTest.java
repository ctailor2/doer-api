package com.doerapispring.todos;

import com.doerapispring.apiTokens.SessionToken;
import com.doerapispring.apiTokens.SessionTokenService;
import com.doerapispring.users.User;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
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
    private SessionTokenService sessionTokenService;

    @Mock
    private TodoRepository todoRepository;

    private ArgumentCaptor<Todo> todoArgumentCaptor = ArgumentCaptor.forClass(Todo.class);
    private TodoEntity todoEntity;

    @Before
    public void setUp() throws Exception {
        todoService = new TodoService(todoRepository, sessionTokenService);
        todoEntity = TodoEntity.builder()
                .task("reconfigure things")
                .build();
    }

    @Test
    public void get_callsSessionTokenRepository_whenTokenExists_callsTodoRepository_returnsTodos() throws Exception {
        SessionToken sessionToken = SessionToken.builder()
                .user(User.builder().id(123L).build())
                .token("tokenz")
                .build();
        List<Todo> todos = Arrays.asList(Todo.builder().task("clean the fridge").build());
        doReturn(sessionToken).when(sessionTokenService).getByToken("tokenz");
        doReturn(todos).when(todoRepository).findByUserId(123L);

        List<TodoEntity> todoEntities = todoService.get("tokenz");

        verify(sessionTokenService).getByToken("tokenz");
        TodoEntity todoEntity = todoEntities.get(0);
        assertThat(todoEntity.getTask()).isEqualTo("clean the fridge");
    }

    @Test
    public void get_callsSessionTokenRepository_whenTokenDoesNotExist_returnsEmptyList() throws Exception {
        doReturn(null).when(sessionTokenService).getByToken("tokenz");

        List<TodoEntity> todoEntities = todoService.get("tokenz");

        verify(sessionTokenService).getByToken("tokenz");
        verifyZeroInteractions(todoRepository);
        assertThat(todoEntities).isEmpty();
    }

    @Test
    public void create_callsSessionTokenRepository_whenTokenExists_callsTodoRepository_savesTodoForUser_returnsTodoEntity() throws Exception {
        User user = User.builder().build();
        SessionToken sessionToken = SessionToken.builder()
                .user(user)
                .token("tokenz")
                .build();

        doReturn(sessionToken).when(sessionTokenService).getByToken("tokenz");

        TodoEntity returnedTodoEntity = todoService.create("tokenz", todoEntity);

        verify(sessionTokenService).getByToken("tokenz");
        verify(todoRepository).save(todoArgumentCaptor.capture());
        Todo savedTodo = todoArgumentCaptor.getValue();
        assertThat(savedTodo.user).isEqualTo(user);
        assertThat(savedTodo.task).isEqualTo("reconfigure things");
        assertThat(savedTodo.createdAt).isToday();
        assertThat(savedTodo.updatedAt).isToday();
        assertThat(returnedTodoEntity).isEqualTo(todoEntity);
    }

    @Test
    public void create_callsSessionTokenRepository_whenTokenDoesNotExist_returnsNull() throws Exception {
        doReturn(null).when(sessionTokenService).getByToken("tokenz");

        TodoEntity returnedTodoEntity = todoService.create("tokenz", todoEntity);

        verify(sessionTokenService).getByToken("tokenz");
        verifyZeroInteractions(todoRepository);
        assertThat(returnedTodoEntity).isNull();
    }
}