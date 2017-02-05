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
import java.util.Optional;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MasterListRepositoryTest {
    private AggregateRootRepository<MasterList, Todo, String> masterListRepository;

    @Mock
    private UserDAO mockUserDAO;

    @Mock
    private TodoDao mockTodoDAO;

    @Captor
    ArgumentCaptor<TodoEntity> todoEntityArgumentCaptor;

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
    public void find_whenThereAreImmediateTodos_returnsMasterListWithTodos() throws Exception {
        String userEmail = "thisUserEmail";
        TodoEntity todoEntity = TodoEntity.builder()
                .userEntity(UserEntity.builder().email(userEmail).build())
                .task("do it now")
                .active(true)
                .position(0)
                .build();
        when(mockTodoDAO.findUnfinishedByUserEmail(any())).thenReturn(Collections.singletonList(todoEntity));

        Optional<MasterList> masterListOptional = masterListRepository.find(new UniqueIdentifier<>("somethingSecret"));

        assertThat(masterListOptional.isPresent()).isTrue();
        MasterList masterList = masterListOptional.get();
        assertThat(masterList.getTodos().size()).isEqualTo(1);
        assertThat(masterList.getTodos()).containsExactly(new Todo(0, "do it now", ScheduledFor.now));
    }

    @Test
    public void find_whenThereArePostponedTodos_returnsMasterListWithTodos() throws Exception {
        String userEmail = "thatUserEmail";
        TodoEntity todoEntity = TodoEntity.builder()
                .userEntity(UserEntity.builder().email(userEmail).build())
                .task("do it later")
                .active(false)
                .position(0)
                .build();
        when(mockTodoDAO.findUnfinishedByUserEmail(any())).thenReturn(Collections.singletonList(todoEntity));

        Optional<MasterList> masterListOptional = masterListRepository.find(new UniqueIdentifier<>("somethingSecret"));

        assertThat(masterListOptional.isPresent()).isTrue();
        MasterList masterList = masterListOptional.get();
        assertThat(masterList.getTodos().size()).isEqualTo(1);
        assertThat(masterList.getTodos()).containsExactly(
                new Todo(0, "do it later", ScheduledFor.later));
    }

    @Test
    public void add_findsUser_whenFound_savesRelationship_setsFields_setsAuditingData() throws Exception {
        UserEntity userEntity = UserEntity.builder().build();
        when(mockUserDAO.findByEmail(any())).thenReturn(userEntity);

        MasterList masterList = MasterList.newEmpty(new UniqueIdentifier("listUserIdentifier"));
        Todo todo = new Todo(1, "bingo", ScheduledFor.later);
        masterListRepository.add(masterList, todo);

        verify(mockUserDAO).findByEmail("listUserIdentifier");
        verify(mockTodoDAO).save(todoEntityArgumentCaptor.capture());
        TodoEntity todoEntity = todoEntityArgumentCaptor.getValue();
        assertThat(todoEntity).isNotNull();
        assertThat(todoEntity.userEntity).isEqualTo(userEntity);
        assertThat(todoEntity.task).isEqualTo("bingo");
        assertThat(todoEntity.position).isEqualTo(1);
        assertThat(todoEntity.createdAt).isToday();
        assertThat(todoEntity.updatedAt).isToday();
    }

    @Test
    public void add_findsUser_whenFound_whenScheduledForNow_correctlyTranslatesScheduling() throws Exception {
        UserEntity userEntity = UserEntity.builder().build();
        when(mockUserDAO.findByEmail(any())).thenReturn(userEntity);

        MasterList masterList = MasterList.newEmpty(new UniqueIdentifier("listUserIdentifier"));
        Todo todo = new Todo(0, "bingo", ScheduledFor.now);
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

        MasterList masterList = MasterList.newEmpty(new UniqueIdentifier("listUserIdentifier"));
        Todo todo = new Todo(0, "bingo", ScheduledFor.later);
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
        Todo todo = new Todo(4, "bingo", ScheduledFor.later);
        MasterList masterList = MasterList.newEmpty(new UniqueIdentifier("nonExistentUser"));
        masterListRepository.add(masterList, todo);

        verify(mockUserDAO).findByEmail("nonExistentUser");
    }

    @Test
    public void remove_findsTodo_whenFound_deletesIt() throws Exception {
        TodoEntity todoEntity = TodoEntity.builder().build();
        when(mockTodoDAO.findUnfinished(any(), any())).thenReturn(todoEntity);

        MasterList masterList = MasterList.newEmpty(new UniqueIdentifier("someUserId"));
        Todo todo = new Todo(0, "bingo", ScheduledFor.later);
        masterListRepository.remove(masterList, todo);

        verify(mockTodoDAO).findUnfinished("someUserId", 0);
        verify(mockTodoDAO).delete(todoEntity);
    }

    @Test
    public void remove_findsTodo_whenNotFound_throwsAbnormalModelException() throws Exception {
        MasterList masterList = MasterList.newEmpty(new UniqueIdentifier("someUserId"));
        Todo todo = new Todo(0, "bingo", ScheduledFor.later);
        when(mockTodoDAO.findUnfinished(any(), any())).thenReturn(null);

        exception.expect(AbnormalModelException.class);
        masterListRepository.remove(masterList, todo);
    }
}