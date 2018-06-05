package com.doerapispring.storage;

import com.doerapispring.domain.MasterList;
import com.doerapispring.domain.ObjectRepository;
import com.doerapispring.domain.Todo;
import com.doerapispring.domain.UniqueIdentifier;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MasterListRepositoryTest {
    private ObjectRepository<MasterList, String> masterListRepository;

    private TodoDao mockTodoDAO;

    private ListUnlockDao mockListUnlockDao;

    private ArgumentCaptor<TodoEntity> todoEntityArgumentCaptor = ArgumentCaptor.forClass(TodoEntity.class);

    @Captor
    private ArgumentCaptor<List<TodoEntity>> todoEntityListArgumentCaptor;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        Clock mockClock = mock(Clock.class);
        mockTodoDAO = mock(TodoDao.class);
        mockListUnlockDao = mock(ListUnlockDao.class);

        when(mockClock.instant()).thenAnswer(invocation -> Instant.now());

        masterListRepository = new MasterListRepository(mockClock, mockTodoDAO, mockListUnlockDao);
    }

    @Test
    public void find_callsTodoDao_callsListUnlockDao() throws Exception {
        masterListRepository.find(new UniqueIdentifier<>("somethingSecret"));

        verify(mockTodoDAO).findUnfinishedByUserEmail("somethingSecret");
        verify(mockListUnlockDao).findFirstUserListUnlock("somethingSecret");
    }

    @Test
    public void find_whenThereAreNoTodos_returnsMasterListWithNoTodos() throws Exception {
        when(mockTodoDAO.findUnfinishedByUserEmail(any())).thenReturn(Collections.emptyList());

        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>("userIdentifier");
        Optional<MasterList> masterListOptional = masterListRepository.find(uniqueIdentifier);

        assertThat(masterListOptional.isPresent()).isTrue();
        MasterList masterList = masterListOptional.get();
        assertThat(masterList.getIdentifier()).isEqualTo(uniqueIdentifier);
        assertThat(masterList.getTodos()).isEmpty();
        masterList.unlock();
        assertThat(masterList.getDeferredTodos()).isEmpty();
    }

    @Test
    public void find_whenThereAreImmediateTodos_returnsMasterListWithTodos() throws Exception {
        String userEmail = "thisUserEmail";
        TodoEntity todoEntity = TodoEntity.builder()
            .uuid("someUuid")
            .userEntity(UserEntity.builder().email(userEmail).build())
            .task("do it now")
            .active(true)
            .position(5)
            .build();
        when(mockTodoDAO.findUnfinishedByUserEmail(any())).thenReturn(singletonList(todoEntity));

        Optional<MasterList> masterListOptional = masterListRepository.find(new UniqueIdentifier<>("somethingSecret"));

        assertThat(masterListOptional.isPresent()).isTrue();
        MasterList masterList = masterListOptional.get();
        assertThat(masterList.getTodos().size()).isEqualTo(1);
        assertThat(masterList.getTodos()).contains(new Todo("someUuid", "do it now", MasterList.NAME, 5));
    }

    @Test
    public void find_whenThereArePostponedTodos_returnsMasterListWithTodos() throws Exception {
        String userEmail = "thatUserEmail";
        TodoEntity todoEntity = TodoEntity.builder()
            .uuid("someUuid")
            .userEntity(UserEntity.builder().email(userEmail).build())
            .task("do it later")
            .active(false)
            .position(5)
            .build();
        when(mockTodoDAO.findUnfinishedByUserEmail(any())).thenReturn(singletonList(todoEntity));

        Optional<MasterList> masterListOptional = masterListRepository.find(new UniqueIdentifier<>("somethingSecret"));

        assertThat(masterListOptional.isPresent()).isTrue();
        MasterList masterList = masterListOptional.get();
        masterList.unlock();
        assertThat(masterList.getDeferredTodos().size()).isEqualTo(1);
        assertThat(masterList.getDeferredTodos()).containsExactly(
            new Todo("someUuid", "do it later", MasterList.DEFERRED_NAME, 5));
    }

    @Test
    public void find_whenThereAreNoListUnlocks_returnsMasterListWithNoListUnlocks() throws Exception {
        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>("soUnique");
        Optional<MasterList> masterListOptional = masterListRepository.find(uniqueIdentifier);

        assertThat(masterListOptional.isPresent()).isTrue();
        MasterList masterList = masterListOptional.get();
        assertThat(masterList.isLocked()).isTrue();
    }

    @Test
    public void find_whenThereIsARecentListUnlock_returnsAnUnlockedMasterList() throws Exception {
        ListUnlockEntity listUnlockEntity = ListUnlockEntity.builder()
            .updatedAt(Date.from(Instant.now().minusSeconds(500)))
            .build();
        when(mockListUnlockDao.findFirstUserListUnlock(any())).thenReturn(listUnlockEntity);

        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>("soUnique");
        Optional<MasterList> masterListOptional = masterListRepository.find(uniqueIdentifier);

        assertThat(masterListOptional.isPresent()).isTrue();
        MasterList masterList = masterListOptional.get();
        assertThat(masterList.isLocked()).isFalse();
    }
}