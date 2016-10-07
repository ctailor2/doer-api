package com.doerapispring.todos;

import com.doerapispring.users.User;
import com.doerapispring.users.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyZeroInteractions;

/**
 * Created by chiragtailor on 9/28/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class TodoServiceTest {
    private TodoService todoService;

    @Mock
    private TodoRepository todoRepository;

    @Mock
    private UserRepository userRepository;

    private ArgumentCaptor<Todo> todoArgumentCaptor = ArgumentCaptor.forClass(Todo.class);
    private TodoEntity todoEntity;

    @Before
    public void setUp() throws Exception {
        todoService = new TodoService(todoRepository, userRepository);
        todoEntity = TodoEntity.builder()
                .task("reconfigure things")
                .build();
    }

    @Test
    public void get_callsTodoRepository_returnsTodos() throws Exception {
        List<Todo> todos = Arrays.asList(Todo.builder().task("clean the fridge").build());
        doReturn(todos).when(todoRepository).findByUserEmail("one@two.com");

        List<TodoEntity> todoEntities = todoService.get("one@two.com");

        TodoEntity todoEntity = todoEntities.get(0);
        assertThat(todoEntity).isNotNull();
        assertThat(todoEntity.getTask()).isEqualTo("clean the fridge");
    }

    @Test
    public void create_callsUserRepository_whenUserExists_callsTodoRepository_savesTodoForUser_returnsTodoEntity() throws Exception {
        User user = User.builder()
                .id(123L)
                .email("one@two.com")
                .build();
        doReturn(user).when(userRepository).findByEmail(user.email);

        TodoEntity returnedTodoEntity = todoService.create(user.email, todoEntity);

        verify(userRepository).findByEmail(user.email);
        verify(todoRepository).save(todoArgumentCaptor.capture());
        Todo savedTodo = todoArgumentCaptor.getValue();
        assertThat(savedTodo.user).isEqualTo(user);
        assertThat(savedTodo.task).isEqualTo("reconfigure things");
        assertThat(savedTodo.createdAt).isToday();
        assertThat(savedTodo.updatedAt).isToday();
        assertThat(returnedTodoEntity).isEqualTo(todoEntity);
    }

    @Test
    public void create_callsUserRepository_whenUserDoesNotExist_returnsNull() throws Exception {
        doReturn(null).when(userRepository).findByEmail("one@two.com");

        TodoEntity returnedTodoEntity = todoService.create("one@two.com", todoEntity);

        verify(userRepository).findByEmail("one@two.com");
        verifyZeroInteractions(todoRepository);
        assertThat(returnedTodoEntity).isNull();
    }
}