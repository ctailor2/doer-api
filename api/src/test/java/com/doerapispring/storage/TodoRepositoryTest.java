package com.doerapispring.storage;

import com.doerapispring.domain.*;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TodoRepositoryTest {
    private AggregateRootRepository<MasterList, Todo> todoRepository;

    private UserDAO mockUserDAO;

    private TodoDao mockTodoDAO;

    private final ArgumentCaptor<TodoEntity> todoEntityArgumentCaptor = ArgumentCaptor.forClass(TodoEntity.class);

    @Captor
    private ArgumentCaptor<List<TodoEntity>> todoEntityListArgumentCaptor;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        mockUserDAO = mock(UserDAO.class);
        mockTodoDAO = mock(TodoDao.class);
        todoRepository = new TodoRepository(mockUserDAO, mockTodoDAO);
    }

    @Test
    public void add_findsUser_whenFound_savesRelationship_setsFields_setsAuditingData() throws Exception {
        UserEntity userEntity = UserEntity.builder().build();
        when(mockUserDAO.findByEmail(any())).thenReturn(userEntity);
        MasterList masterList = new MasterList(Clock.systemDefaultZone(), new UniqueIdentifier<>("listUserIdentifier"), null);
        Todo todo = new Todo("someId", "bingo", MasterList.DEFERRED_NAME, 3);
        todoRepository.add(masterList, todo);

        verify(mockUserDAO).findByEmail("listUserIdentifier");
        verify(mockTodoDAO).save(todoEntityArgumentCaptor.capture());
        TodoEntity todoEntity = todoEntityArgumentCaptor.getValue();
        assertThat(todoEntity).isNotNull();
        assertThat(todoEntity.uuid).isEqualTo("someId");
        assertThat(todoEntity.userEntity).isEqualTo(userEntity);
        assertThat(todoEntity.task).isEqualTo("bingo");
        assertThat(todoEntity.position).isEqualTo(3);
        assertThat(todoEntity.createdAt).isToday();
        assertThat(todoEntity.updatedAt).isToday();
    }

    @Test
    public void add_findsUser_whenFound_whenScheduledForNow_correctlyTranslatesScheduling() throws Exception {
        UserEntity userEntity = UserEntity.builder().build();
        when(mockUserDAO.findByEmail(any())).thenReturn(userEntity);
        MasterList masterList = new MasterList(Clock.systemDefaultZone(), new UniqueIdentifier<>("listUserIdentifier"), null);
        Todo todo = new Todo("someId", "bingo", MasterList.NAME, 5);
        todoRepository.add(masterList, todo);

        verify(mockUserDAO).findByEmail("listUserIdentifier");
        verify(mockTodoDAO).save(todoEntityArgumentCaptor.capture());
        TodoEntity todoEntity = todoEntityArgumentCaptor.getValue();
        assertThat(todoEntity).isNotNull();
        assertThat(todoEntity.active).isTrue();
    }

    @Test
    public void add_findsUser_whenFound_whenScheduledForLater_correctlyTranslatesScheduling() throws Exception {
        UserEntity userEntity = UserEntity.builder().build();
        when(mockUserDAO.findByEmail(any())).thenReturn(userEntity);
        MasterList masterList = new MasterList(Clock.systemDefaultZone(), new UniqueIdentifier<>("listUserIdentifier"), null);
        Todo todo = new Todo("someId", "bingo", MasterList.DEFERRED_NAME, 5);
        todoRepository.add(masterList, todo);

        verify(mockUserDAO).findByEmail("listUserIdentifier");
        verify(mockTodoDAO).save(todoEntityArgumentCaptor.capture());
        TodoEntity todoEntity = todoEntityArgumentCaptor.getValue();
        assertThat(todoEntity).isNotNull();
        assertThat(todoEntity.active).isFalse();
    }

    @Test
    public void add_findsUser_whenNotFound_throwsAbnormalModelException() throws Exception {
        when(mockUserDAO.findByEmail(any())).thenReturn(null);

        exception.expect(AbnormalModelException.class);
        Todo todo = new Todo("someId", "bingo", MasterList.DEFERRED_NAME, 5);
        MasterList masterList = new MasterList(Clock.systemDefaultZone(), new UniqueIdentifier<>("listUserIdentifier"), null);
        todoRepository.add(masterList, todo);

        verify(mockUserDAO).findByEmail("listUserIdentifier");
    }

    @Test
    public void remove_findsTodo_whenFound_deletesIt() throws Exception {
        TodoEntity todoEntity = TodoEntity.builder().build();
        when(mockTodoDAO.findUserTodo(any(), any())).thenReturn(todoEntity);
        MasterList masterList = new MasterList(Clock.systemDefaultZone(), new UniqueIdentifier<>("listUserIdentifier"), null);
        Todo todo = new Todo("someUuid", "bingo", MasterList.DEFERRED_NAME, 5);
        todoRepository.remove(masterList, todo);

        verify(mockTodoDAO).findUserTodo("listUserIdentifier", "someUuid");
        verify(mockTodoDAO).delete(todoEntity);
    }

    @Test
    public void remove_findsTodo_whenNotFound_throwsAbnormalModelException() throws Exception {
        MasterList masterList = new MasterList(Clock.systemDefaultZone(), new UniqueIdentifier<>("listUserIdentifier"), null);
        Todo todo = new Todo("someUuid", "bingo", MasterList.NAME, 5);
        when(mockTodoDAO.findUserTodo(any(), any())).thenReturn(null);

        exception.expect(AbnormalModelException.class);
        todoRepository.remove(masterList, todo);

        verify(mockTodoDAO).findUserTodo("listUserIdentifier", "someUuid");
    }

    @Test
    public void update_findsTodo_whenFound_updatesTodo() throws Exception {
        TodoEntity existingTodoEntity = TodoEntity.builder()
            .uuid("someUuid")
            .userEntity(UserEntity.builder().build())
            .createdAt(new Date())
            .build();
        when(mockTodoDAO.findUserTodo(any(), any())).thenReturn(existingTodoEntity);

        MasterList masterList = new MasterList(Clock.systemDefaultZone(), new UniqueIdentifier<>("listUserIdentifier"), null);
        Todo todo = new Todo("someUuid", "bingo", MasterList.DEFERRED_NAME, 5);
        todoRepository.update(masterList, todo);

        verify(mockTodoDAO).findUserTodo("listUserIdentifier", "someUuid");
        verify(mockTodoDAO).save(todoEntityArgumentCaptor.capture());
        TodoEntity todoEntity = todoEntityArgumentCaptor.getValue();
        assertThat(todoEntity.uuid).isEqualTo(existingTodoEntity.uuid);
        assertThat(todoEntity.userEntity).isEqualTo(existingTodoEntity.userEntity);
        assertThat(todoEntity.task).isEqualTo("bingo");
        assertThat(todoEntity.active).isFalse();
        assertThat(todoEntity.position).isEqualTo(5);
        assertThat(todoEntity.createdAt).isEqualTo(existingTodoEntity.createdAt);
        assertThat(todoEntity.updatedAt).isToday();
    }

    @Test
    public void update_findsTodo_whenFound_whenTodoIsComplete_updatesTodo() throws Exception {
        TodoEntity existingTodoEntity = TodoEntity.builder()
            .uuid("someUuid")
            .completed(false)
            .build();
        when(mockTodoDAO.findUserTodo(any(), any())).thenReturn(existingTodoEntity);

        MasterList masterList = new MasterList(Clock.systemDefaultZone(), new UniqueIdentifier<>("listUserIdentifier"), null);
        Todo todo = new Todo("someUuid", "bingo", MasterList.DEFERRED_NAME, 5);
        todo.complete();
        todoRepository.update(masterList, todo);

        verify(mockTodoDAO).findUserTodo("listUserIdentifier", "someUuid");
        verify(mockTodoDAO).save(todoEntityArgumentCaptor.capture());
        TodoEntity todoEntity = todoEntityArgumentCaptor.getValue();
        assertThat(todoEntity.completed).isEqualTo(true);
    }

    @Test
    public void update_findsTodo_whenNotFound_throwsAbnormalModelException() throws Exception {
        when(mockTodoDAO.findUserTodo(any(), any())).thenReturn(null);

        exception.expect(AbnormalModelException.class);
        Todo todo = new Todo("someUuid", "bingo", MasterList.DEFERRED_NAME, 5);
        MasterList masterList = new MasterList(Clock.systemDefaultZone(), new UniqueIdentifier<>("listUserIdentifier"), null);
        todoRepository.update(masterList, todo);

        verify(mockTodoDAO).findUserTodo("listUserIdentifier", "someUuid");
    }

    @Test
    public void update_multipleTodos_findsEachTodo_andUpdatesIt() throws Exception {
        when(mockTodoDAO.findUserTodo(any(), any())).thenReturn(TodoEntity.builder().build());
        List<Todo> todos = asList(
            new Todo("uuid1", "bingo", MasterList.DEFERRED_NAME, 5),
            new Todo("uuid2", "bango", MasterList.DEFERRED_NAME, 2));
        MasterList masterList = new MasterList(Clock.systemDefaultZone(), new UniqueIdentifier<>("listUserIdentifier"), null);

        todoRepository.update(masterList, todos);

        ArgumentCaptor<String> idArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(mockTodoDAO, times(2)).findUserTodo(eq("listUserIdentifier"), idArgumentCaptor.capture());
        assertThat(idArgumentCaptor.getAllValues()).isEqualTo(asList("uuid1", "uuid2"));

        verify(mockTodoDAO).save(todoEntityListArgumentCaptor.capture());
        List<TodoEntity> savedTodos = todoEntityListArgumentCaptor.getValue();
        assertThat(savedTodos.size()).isEqualTo(2);
        TodoEntity firstTodoEntity = savedTodos.get(0);
        assertThat(firstTodoEntity.task).isEqualTo("bingo");
        assertThat(firstTodoEntity.position).isEqualTo(5);
        TodoEntity secondTodoEntity = savedTodos.get(1);
        assertThat(secondTodoEntity.task).isEqualTo("bango");
        assertThat(secondTodoEntity.position).isEqualTo(2);
    }

    @Test
    public void update_multipleTodos_whenAnyTodoIsNotFound_throwsAbnormalModelException_doesNotUpdate() throws Exception {
        when(mockTodoDAO.findUserTodo(any(), any())).thenReturn(TodoEntity.builder().build());
        when(mockTodoDAO.findUserTodo(any(), eq("uuid2"))).thenReturn(null);
        List<Todo> todos = asList(
            new Todo("uuid1", "bingo", MasterList.DEFERRED_NAME, 5),
            new Todo("uuid2", "bango", MasterList.DEFERRED_NAME, 2));
        MasterList masterList = new MasterList(Clock.systemDefaultZone(), new UniqueIdentifier<>("listUserIdentifier"), null);

        exception.expect(AbnormalModelException.class);
        todoRepository.update(masterList, todos);

        verify(mockTodoDAO, times(2)).findUserTodo(any(), any());
        verifyNoMoreInteractions(mockTodoDAO);
    }
}