package com.doerapispring.todos;

import com.doerapispring.AbnormalModelException;
import com.doerapispring.UserIdentifier;
import com.doerapispring.users.UserDAO;
import com.doerapispring.users.UserEntity;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by chiragtailor on 11/24/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class TodoRepositoryTest {
    private TodoRepository todoRepository;

    @Mock
    private TodoDao todoDao;

    @Mock
    private UserDAO userDao;

    @Captor
    ArgumentCaptor<TodoEntity> todoEntityArgumentCaptor;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        todoRepository = new TodoRepository(userDao, todoDao);
    }

    @Test
    public void add_todo_findsUser_whenFound_savesRelationship_setsFields_setsAuditingData() throws Exception {
        UserEntity userEntity = UserEntity.builder().build();
        when(userDao.findByEmail(any())).thenReturn(userEntity);

        todoRepository.add(new Todo(
                new UserIdentifier("somethingIdentifying"),
                "do it",
                ScheduledFor.now));

        verify(userDao).findByEmail("somethingIdentifying");
        verify(todoDao).save(todoEntityArgumentCaptor.capture());
        TodoEntity todoEntity = todoEntityArgumentCaptor.getValue();
        assertThat(todoEntity).isNotNull();
        assertThat(todoEntity.userEntity).isEqualTo(userEntity);
        assertThat(todoEntity.task).isEqualTo("do it");
        assertThat(todoEntity.createdAt).isToday();
        assertThat(todoEntity.updatedAt).isToday();
    }

    @Test
    public void add_todo_findsUser_whenFound_whenScheduledForNow_correctlyTranslatesScheduling() throws Exception {
        UserEntity userEntity = UserEntity.builder().build();
        when(userDao.findByEmail(any())).thenReturn(userEntity);

        todoRepository.add(new Todo(
                new UserIdentifier("somethingIdentifying"),
                "do it",
                ScheduledFor.now));
        verify(todoDao).save(todoEntityArgumentCaptor.capture());
        TodoEntity todoEntity = todoEntityArgumentCaptor.getValue();
        assertThat(todoEntity.active).isTrue();
    }


    @Test
    public void add_todo_findsUser_whenFound_whenScheduledForLater_correctlyTranslatesScheduling() throws Exception {
        UserEntity userEntity = UserEntity.builder().build();
        when(userDao.findByEmail(any())).thenReturn(userEntity);

        todoRepository.add(new Todo(
                new UserIdentifier("somethingIdentifying"),
                "do it",
                ScheduledFor.later));
        verify(todoDao).save(todoEntityArgumentCaptor.capture());
        TodoEntity todoEntity = todoEntityArgumentCaptor.getValue();
        assertThat(todoEntity.active).isFalse();
    }

    @Test
    public void add_todo_findsUser_whenNotFound_throwsAbnormalModelException() throws Exception {
        when(userDao.findByEmail(any())).thenReturn(null);

        exception.expect(AbnormalModelException.class);
        todoRepository.add(new Todo(
                new UserIdentifier("somethingIdentifying"),
                "do it",
                null));

        verify(userDao).findByEmail("somethingIdentifying");
    }

    @Test
    public void findByScheduling_whenScheduledForAnytime_findsAllUserTodos() throws Exception {
        todoRepository.findByScheduling(new UserIdentifier("something"), ScheduledFor.anytime);

        verify(todoDao).findByUserEmail("something");
    }

    @Test
    public void findByScheduling_returnsTodo() throws Exception {
        when(todoDao.findByUserEmail(any())).thenReturn(Collections.singletonList(
                TodoEntity.builder().task("task 1").active(true).build()));

        UserIdentifier userIdentifier = new UserIdentifier("something");
        List<Todo> todos = todoRepository.findByScheduling(userIdentifier, ScheduledFor.anytime);

        assertThat(todos.size()).isEqualTo(1);
        Todo firstTodo = todos.get(0);
        assertThat(firstTodo.getUserIdentifier()).isEqualTo(userIdentifier);
        assertThat(firstTodo.getTask()).isEqualTo("task 1");
        assertThat(firstTodo.getScheduling()).isEqualTo(ScheduledFor.now);
    }

    @Test
    public void findByScheduling_whenScheduledForLater_findsUserTodos_scheduledForLater() throws Exception {
        todoRepository.findByScheduling(new UserIdentifier("something"), ScheduledFor.later);

        verify(todoDao).findByUserEmailAndActiveStatus("something", false);
    }

    @Test
    public void findByScheduling_whenScheduledForNow_findsUserTodos_scheduledForLater() throws Exception {
        todoRepository.findByScheduling(new UserIdentifier("something"), ScheduledFor.now);

        verify(todoDao).findByUserEmailAndActiveStatus("something", true);
    }
}