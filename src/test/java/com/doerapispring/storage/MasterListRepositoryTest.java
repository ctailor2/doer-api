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
    private UserDAO userDAO;

    @Mock
    private TodoDao todoDao;

    @Captor
    ArgumentCaptor<TodoEntity> todoEntityArgumentCaptor;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        masterListRepository = new MasterListRepository(userDAO, todoDao);
    }

    @Test
    public void find_callsTodoDao() throws Exception {
        masterListRepository.find(new UserIdentifier("somethingSecret"));

        verify(todoDao).findByUserEmail("somethingSecret");
    }

    @Test
    public void find_whenThereAreNoTodos_returnsMasterListWithNoTodos() throws Exception {
        when(todoDao.findByUserEmail(any())).thenReturn(Collections.emptyList());

        UserIdentifier userIdentifier = new UserIdentifier("userIdentifier");
        Optional<MasterList> masterListOptional = masterListRepository.find(userIdentifier);

        assertThat(masterListOptional.isPresent()).isTrue();
        MasterList masterList = masterListOptional.get();
        assertThat(masterList.getIdentifier()).isEqualTo(userIdentifier);
        assertThat(masterList.getImmediateList()).isNotNull();
        assertThat(masterList.getPostponedList()).isNotNull();
        assertThat(masterList.getImmediateList().getTodos().size()).isEqualTo(0);
        assertThat(masterList.getPostponedList().getTodos().size()).isEqualTo(0);
    }

    @Test
    public void find_whenThereAreImmediateTodos_returnsMasterListWithTodosInCorrespondingLists() throws Exception {
        String userEmail = "thisUserEmail";
        TodoEntity todoEntity = TodoEntity.builder()
                .userEntity(UserEntity.builder().email(userEmail).build())
                .task("do it now")
                .active(true)
                .build();
        when(todoDao.findByUserEmail(any())).thenReturn(Collections.singletonList(todoEntity));

        Optional<MasterList> masterListOptional = masterListRepository.find(new UserIdentifier("somethingSecret"));

        assertThat(masterListOptional.isPresent()).isTrue();
        MasterList masterList = masterListOptional.get();
        assertThat(masterList.getImmediateList().getTodos().size()).isEqualTo(1);
        assertThat(masterList.getImmediateList().getTodos().get(0))
                .isEqualTo(new Todo("do it now", ScheduledFor.now));
    }

    @Test
    public void find_whenThereArePostponedTodos_returnsMasterListWithTodosInCorrespondingLists() throws Exception {
        String userEmail = "thatUserEmail";
        TodoEntity todoEntity = TodoEntity.builder()
                .userEntity(UserEntity.builder().email(userEmail).build())
                .task("do it later")
                .active(false)
                .build();
        when(todoDao.findByUserEmail(any())).thenReturn(Collections.singletonList(todoEntity));

        Optional<MasterList> masterListOptional = masterListRepository.find(new UserIdentifier("somethingSecret"));

        assertThat(masterListOptional.isPresent()).isTrue();
        MasterList masterList = masterListOptional.get();
        assertThat(masterList.getPostponedList().getTodos().size()).isEqualTo(1);
        assertThat(masterList.getPostponedList().getTodos().get(0))
                .isEqualTo(new Todo("do it later", ScheduledFor.later));
    }

    @Test
    public void add_findsUser_whenFound_savesRelationship_setsFields_setsAuditingData() throws Exception {
        UserEntity userEntity = UserEntity.builder().build();
        when(userDAO.findByEmail(any())).thenReturn(userEntity);

        MasterList masterList = new MasterList(new UserIdentifier("listUserIdentifier"), null, null);
        Todo todo = new Todo("bingo", ScheduledFor.later);
        masterListRepository.add(masterList, todo);

        verify(userDAO).findByEmail("listUserIdentifier");
        verify(todoDao).save(todoEntityArgumentCaptor.capture());
        TodoEntity todoEntity = todoEntityArgumentCaptor.getValue();
        assertThat(todoEntity).isNotNull();
        assertThat(todoEntity.userEntity).isEqualTo(userEntity);
        assertThat(todoEntity.task).isEqualTo("bingo");
        assertThat(todoEntity.createdAt).isToday();
        assertThat(todoEntity.updatedAt).isToday();
    }

    @Test
    public void add_findsUser_whenFound_whenScheduledForNow_correctlyTranslatesScheduling() throws Exception {
        UserEntity userEntity = UserEntity.builder().build();
        when(userDAO.findByEmail(any())).thenReturn(userEntity);

        MasterList masterList = new MasterList(new UserIdentifier("listUserIdentifier"), null, null);
        Todo todo = new Todo("bingo", ScheduledFor.now);
        masterListRepository.add(masterList, todo);

        verify(userDAO).findByEmail("listUserIdentifier");
        verify(todoDao).save(todoEntityArgumentCaptor.capture());
        TodoEntity todoEntity = todoEntityArgumentCaptor.getValue();
        assertThat(todoEntity).isNotNull();
        assertThat(todoEntity.active).isTrue();
    }

    @Test
    public void add_findsUser_whenFound_whenScheduledForLater_correctlyTranslatesScheduling() throws Exception {
        UserEntity userEntity = UserEntity.builder().build();
        when(userDAO.findByEmail(any())).thenReturn(userEntity);

        MasterList masterList = new MasterList(new UserIdentifier("listUserIdentifier"), null, null);
        Todo todo = new Todo("bingo", ScheduledFor.later);
        masterListRepository.add(masterList, todo);

        verify(userDAO).findByEmail("listUserIdentifier");
        verify(todoDao).save(todoEntityArgumentCaptor.capture());
        TodoEntity todoEntity = todoEntityArgumentCaptor.getValue();
        assertThat(todoEntity).isNotNull();
        assertThat(todoEntity.active).isFalse();
    }

    @Test
    public void add_findsUser_whenNotFound_throwsAbnormalModelException() throws Exception {
        when(userDAO.findByEmail(any())).thenReturn(null);

        exception.expect(AbnormalModelException.class);
        Todo todo = new Todo("bingo", ScheduledFor.later);
        MasterList masterList = new MasterList(new UserIdentifier("nonExistentUser"), null, null);
        masterListRepository.add(masterList, todo);

        verify(userDAO).findByEmail("nonExistentUser");
    }
}