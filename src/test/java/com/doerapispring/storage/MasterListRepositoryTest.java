package com.doerapispring.storage;

import com.doerapispring.domain.*;
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
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MasterListRepositoryTest {
    private AggregateRootRepository<MasterList, Todo, String> masterListRepository;

    @Mock
    private UserDAO mockUserDAO;

    @Mock
    private TodoDao mockTodoDAO;

    @Captor
    ArgumentCaptor<TodoEntity> todoEntityArgumentCaptor;

    @Captor
    ArgumentCaptor<List<TodoEntity>> todoEntityListArgumentCaptor;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        masterListRepository = new MasterListRepository(mockUserDAO, mockTodoDAO);
    }

    @Test
    public void find_callsTodoDao() throws Exception {
        masterListRepository.find(new UniqueIdentifier<>("somethingSecret"));

        verify(mockTodoDAO).findUnfinishedByUserEmail("somethingSecret");
    }

    @Test
    public void find_whenThereAreNoTodos_returnsMasterListWithNoTodos() throws Exception {
        when(mockTodoDAO.findUnfinishedByUserEmail(any())).thenReturn(Collections.emptyList());

        UniqueIdentifier uniqueIdentifier = new UniqueIdentifier<>("userIdentifier");
        Optional<MasterList> masterListOptional = masterListRepository.find(uniqueIdentifier);

        assertThat(masterListOptional.isPresent()).isTrue();
        MasterList masterList = masterListOptional.get();
        assertThat(masterList.getIdentifier()).isEqualTo(uniqueIdentifier);
        assertThat(masterList.getTodos()).isEmpty();
    }

    @Test
    public void find_whenThereAreTodos_returnsMasterListWithTodos() throws Exception {
        String userEmail = "thisUserEmail";
        TodoEntity todoEntity = TodoEntity.builder()
                .id(123L)
                .userEntity(UserEntity.builder().email(userEmail).build())
                .task("do it now")
                .active(true)
                .position(5)
                .build();
        when(mockTodoDAO.findUnfinishedByUserEmail(any())).thenReturn(Collections.singletonList(todoEntity));

        Optional<MasterList> masterListOptional = masterListRepository.find(new UniqueIdentifier<>("somethingSecret"));

        assertThat(masterListOptional.isPresent()).isTrue();
        MasterList masterList = masterListOptional.get();
        assertThat(masterList.getTodos().size()).isEqualTo(1);
        assertThat(masterList.getTodos()).contains(new Todo("123", "do it now", ScheduledFor.now, 5));
    }

    @Test
    public void find_whenThereArePostponedTodos_returnsMasterListWithTodos() throws Exception {
        String userEmail = "thatUserEmail";
        TodoEntity todoEntity = TodoEntity.builder()
                .id(123L)
                .userEntity(UserEntity.builder().email(userEmail).build())
                .task("do it later")
                .active(false)
                .position(5)
                .build();
        when(mockTodoDAO.findUnfinishedByUserEmail(any())).thenReturn(Collections.singletonList(todoEntity));

        Optional<MasterList> masterListOptional = masterListRepository.find(new UniqueIdentifier<>("somethingSecret"));

        assertThat(masterListOptional.isPresent()).isTrue();
        MasterList masterList = masterListOptional.get();
        assertThat(masterList.getTodos().size()).isEqualTo(1);
        assertThat(masterList.getTodos()).containsExactly(
                new Todo("123", "do it later", ScheduledFor.later, 5));
    }

    @Test
    public void add_findsUser_whenFound_savesRelationship_setsFields_setsAuditingData() throws Exception {
        UserEntity userEntity = UserEntity.builder().build();
        when(mockUserDAO.findByEmail(any())).thenReturn(userEntity);

        MasterList masterList = new MasterList(new UniqueIdentifier("listUserIdentifier"), 2);
        Todo todo = new Todo("bingo", ScheduledFor.later, 3);
        masterListRepository.add(masterList, todo);

        verify(mockUserDAO).findByEmail("listUserIdentifier");
        verify(mockTodoDAO).save(todoEntityArgumentCaptor.capture());
        TodoEntity todoEntity = todoEntityArgumentCaptor.getValue();
        assertThat(todoEntity).isNotNull();
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

        MasterList masterList = new MasterList(new UniqueIdentifier("listUserIdentifier"), 2);
        Todo todo = new Todo("bingo", ScheduledFor.now, 5);
        masterListRepository.add(masterList, todo);

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

        MasterList masterList = new MasterList(new UniqueIdentifier("listUserIdentifier"), 2);
        Todo todo = new Todo("bingo", ScheduledFor.later, 5);
        masterListRepository.add(masterList, todo);

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
        Todo todo = new Todo("bingo", ScheduledFor.later, 5);
        MasterList masterList = new MasterList(new UniqueIdentifier("nonExistentUser"), 2);
        masterListRepository.add(masterList, todo);

        verify(mockUserDAO).findByEmail("nonExistentUser");
    }

    @Test
    public void remove_findsTodo_whenFound_deletesIt() throws Exception {
        TodoEntity todoEntity = TodoEntity.builder().build();
        when(mockTodoDAO.findUserTodo(any(), any())).thenReturn(todoEntity);

        MasterList masterList = new MasterList(new UniqueIdentifier("someUserId"), 2);
        Todo todo = new Todo("123", "bingo", ScheduledFor.later, 5);
        masterListRepository.remove(masterList, todo);

        verify(mockTodoDAO).findUserTodo("someUserId", 123L);
        verify(mockTodoDAO).delete(todoEntity);
    }

    @Test
    public void remove_findsTodo_whenNotFound_throwsAbnormalModelException() throws Exception {
        MasterList masterList = new MasterList(new UniqueIdentifier("someUserId"), 2);
        Todo todo = new Todo("123", "bingo", ScheduledFor.now, 5);
        when(mockTodoDAO.findUserTodo(any(), any())).thenReturn(null);

        exception.expect(AbnormalModelException.class);
        masterListRepository.remove(masterList, todo);

        verify(mockTodoDAO).findUserTodo("someUserId", 123L);
    }

    @Test
    public void update_findsTodo_whenFound_updatesTodo() throws Exception {
        TodoEntity existingTodoEntity = TodoEntity.builder()
                .id(123L)
                .userEntity(UserEntity.builder().build())
                .createdAt(new Date())
                .build();
        when(mockTodoDAO.findUserTodo(any(), any())).thenReturn(existingTodoEntity);

        MasterList masterList = new MasterList(new UniqueIdentifier("someUserId"), 2);
        Todo todo = new Todo("123", "bingo", ScheduledFor.later, 5);
        masterListRepository.update(masterList, todo);

        verify(mockTodoDAO).findUserTodo("someUserId", 123L);
        verify(mockTodoDAO).save(todoEntityArgumentCaptor.capture());
        TodoEntity todoEntity = todoEntityArgumentCaptor.getValue();
        assertThat(todoEntity.id).isEqualTo(existingTodoEntity.id);
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
                .id(123L)
                .completed(false)
                .build();
        when(mockTodoDAO.findUserTodo(any(), any())).thenReturn(existingTodoEntity);

        MasterList masterList = new MasterList(new UniqueIdentifier("someUserId"), 2);
        Todo todo = new Todo("123", "bingo", ScheduledFor.later, 5);
        todo.complete();
        masterListRepository.update(masterList, todo);

        verify(mockTodoDAO).findUserTodo("someUserId", 123L);
        verify(mockTodoDAO).save(todoEntityArgumentCaptor.capture());
        TodoEntity todoEntity = todoEntityArgumentCaptor.getValue();
        assertThat(todoEntity.completed).isEqualTo(true);
    }

    @Test
    public void update_findsTodo_whenNotFound_throwsAbnormalModelException() throws Exception {
        when(mockTodoDAO.findUserTodo(any(), any())).thenReturn(null);

        exception.expect(AbnormalModelException.class);
        Todo todo = new Todo("bingo", ScheduledFor.later, 5);
        MasterList masterList = new MasterList(new UniqueIdentifier("nonExistentUser"), 2);
        masterListRepository.update(masterList, todo);

        verify(mockTodoDAO).findUserTodo("nonExistentUser", 5L);
    }

    @Test
    public void update_multipleTodos_findsEachTodo_andUpdatesIt() throws Exception {
        when(mockTodoDAO.findUserTodo(any(), any())).thenReturn(TodoEntity.builder().build());
        List<Todo> todos = asList(new Todo("123", "bingo", ScheduledFor.later, 5),
                new Todo("456", "bango", ScheduledFor.later, 2));
        MasterList masterList = new MasterList(new UniqueIdentifier("someUserId"), 2);

        masterListRepository.update(masterList, todos);

        ArgumentCaptor<Long> idArgumentCaptor = ArgumentCaptor.forClass(Long.class);
        verify(mockTodoDAO, times(2)).findUserTodo(eq("someUserId"), idArgumentCaptor.capture());
        assertThat(idArgumentCaptor.getAllValues()).isEqualTo(asList(123L, 456L));

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
        when(mockTodoDAO.findUserTodo(any(), eq(456L))).thenReturn(null);
        List<Todo> todos = asList(new Todo("123", "bingo", ScheduledFor.later, 5),
                new Todo("456", "bango", ScheduledFor.later, 2));
        MasterList masterList = new MasterList(new UniqueIdentifier("someUserId"), 2);

        exception.expect(AbnormalModelException.class);
        masterListRepository.update(masterList, todos);

        verify(mockTodoDAO, times(2)).findUserTodo(any(), any());
        verifyNoMoreInteractions(mockTodoDAO);
    }
}