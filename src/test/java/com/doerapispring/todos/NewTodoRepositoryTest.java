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

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by chiragtailor on 11/24/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class NewTodoRepositoryTest {
    private NewTodoRepository newTodoRepository;

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
        newTodoRepository = new NewTodoRepository(userDao, todoDao);
    }

    @Test
    public void add_todo_findsUser_whenFound_savesRelationship_setsFields_setsAuditingData() throws Exception {
        UserEntity userEntity = UserEntity.builder().build();
        when(userDao.findByEmail(any())).thenReturn(userEntity);

        newTodoRepository.add(new NewTodo(
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

        newTodoRepository.add(new NewTodo(
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

        newTodoRepository.add(new NewTodo(
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
        newTodoRepository.add(new NewTodo(
                new UserIdentifier("somethingIdentifying"),
                "do it",
                null));

        verify(userDao).findByEmail("somethingIdentifying");
    }
}