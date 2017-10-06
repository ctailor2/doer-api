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

import java.time.Clock;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class MasterListRepositoryTest {
    private ObjectRepository<MasterList, String> masterListRepository;

    @Mock
    private TodoDao mockTodoDAO;

    @Mock
    private ListUnlockDao mockListUnlockDao;

    @Mock
    private Clock mockClock;

    @Captor
    ArgumentCaptor<TodoEntity> todoEntityArgumentCaptor;
    @Captor
    ArgumentCaptor<List<TodoEntity>> todoEntityListArgumentCaptor;
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        masterListRepository = new MasterListRepository(mockClock, mockTodoDAO, mockListUnlockDao);
    }

    @Test
    public void find_callsTodoDao_callsListUnlockDao() throws Exception {
        masterListRepository.find(new UniqueIdentifier<>("somethingSecret"));

        verify(mockTodoDAO).findUnfinishedByUserEmail("somethingSecret");
        verify(mockListUnlockDao).findAllUserListUnlocks("somethingSecret");
    }

    @Test
    public void find_whenThereAreNoTodos_returnsMasterListWithNoTodos() throws Exception {
        when(mockTodoDAO.findUnfinishedByUserEmail(any())).thenReturn(Collections.emptyList());

        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>("userIdentifier");
        Optional<MasterList> masterListOptional = masterListRepository.find(uniqueIdentifier);

        assertThat(masterListOptional.isPresent()).isTrue();
        MasterList masterList = masterListOptional.get();
        assertThat(masterList.getIdentifier()).isEqualTo(uniqueIdentifier);
        assertThat(masterList.getAllTodos()).isEmpty();
    }

    @Test
    public void find_whenThereAreImmediateTodos_returnsMasterListWithTodos() throws Exception {
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
        assertThat(masterList.getAllTodos().size()).isEqualTo(1);
        assertThat(masterList.getAllTodos()).contains(new Todo("123", "do it now", MasterList.NAME, 5));
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
        assertThat(masterList.getAllTodos().size()).isEqualTo(1);
        assertThat(masterList.getAllTodos()).containsExactly(
                new Todo("123", "do it later", MasterList.DEFERRED_NAME, 5));
    }

    @Test
    public void find_whenThereAreNoListUnlocks_returnsMasterListWithNoListUnlocks() throws Exception {
        UniqueIdentifier uniqueIdentifier = new UniqueIdentifier<>("soUnique");
        Optional<MasterList> masterListOptional = masterListRepository.find(uniqueIdentifier);

        assertThat(masterListOptional.isPresent()).isTrue();
        MasterList masterList = masterListOptional.get();
        assertThat(masterList.getListUnlocks()).isEmpty();
    }

    @Test
    public void find_whenThereAreListUnlocks_returnsMasterListWithListUnlocks() throws Exception {
        List<ListUnlockEntity> listUnlockEntities = Collections.singletonList(
            ListUnlockEntity.builder()
                .updatedAt(new Date(0L))
                .build());
        when(mockListUnlockDao.findAllUserListUnlocks(any())).thenReturn(listUnlockEntities);

        UniqueIdentifier uniqueIdentifier = new UniqueIdentifier<>("soUnique");
        Optional<MasterList> masterListOptional = masterListRepository.find(uniqueIdentifier);

        assertThat(masterListOptional.isPresent()).isTrue();
        MasterList masterList = masterListOptional.get();
        assertThat(masterList.getListUnlocks()).contains(new ListUnlock(new Date(0)));
    }
}