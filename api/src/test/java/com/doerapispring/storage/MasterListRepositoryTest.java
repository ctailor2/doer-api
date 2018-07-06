package com.doerapispring.storage;

import com.doerapispring.domain.*;
import org.fest.assertions.api.Assertions;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;
import java.util.Optional;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MasterListRepositoryTest {
    private ObjectRepository<MasterList, String> masterListRepository;
    @Rule
    public ExpectedException exception = ExpectedException.none();
    private MasterListDao masterListDao;
    private UserDAO userDao;

    @Before
    public void setUp() throws Exception {
        masterListDao = mock(MasterListDao.class);
        userDao = mock(UserDAO.class);
        masterListRepository = new MasterListRepository(userDao, masterListDao, Clock.systemDefaultZone());

        when(masterListDao.findByEmail(anyString())).thenReturn(new MasterListEntity());
    }

    @Test
    public void find_callsMasterListDao() throws Exception {
        masterListRepository.find(new UniqueIdentifier<>("somethingSecret"));

        verify(masterListDao).findByEmail("somethingSecret");
    }

    @Test
    public void find_whenThereAreNoTodos_returnsMasterListWithNoTodos() throws Exception {
        when(masterListDao.findByEmail(anyString())).thenReturn(MasterListEntity.builder().email("thatUserEmail").todoEntities(emptyList()).build());

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
        when(masterListDao.findByEmail(anyString())).thenReturn(MasterListEntity.builder().email(userEmail).todoEntities(singletonList(todoEntity)).build());

        Optional<MasterList> masterListOptional = masterListRepository.find(new UniqueIdentifier<>("somethingSecret"));

        assertThat(masterListOptional.isPresent()).isTrue();
        MasterList masterList = masterListOptional.get();
        assertThat(masterList.getTodos().size()).isEqualTo(1);
        Assertions.assertThat(masterList.getTodos()).contains(new Todo("someUuid", "do it now", MasterList.NAME, 5));
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
        when(masterListDao.findByEmail(anyString())).thenReturn(MasterListEntity.builder().email(userEmail).todoEntities(singletonList(todoEntity)).build());

        Optional<MasterList> masterListOptional = masterListRepository.find(new UniqueIdentifier<>("somethingSecret"));

        assertThat(masterListOptional.isPresent()).isTrue();
        MasterList masterList = masterListOptional.get();
        masterList.unlock();
        assertThat(masterList.getDeferredTodos().size()).isEqualTo(1);
        assertThat(masterList.getDeferredTodos()).containsExactly(
            new Todo("someUuid", "do it later", MasterList.DEFERRED_NAME, 5));
    }

    @Test
    public void find_whenThereAreNoListUnlocks_returnsALockedMasterList() throws Exception {
        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>("soUnique");
        Optional<MasterList> masterListOptional = masterListRepository.find(uniqueIdentifier);

        assertThat(masterListOptional.isPresent()).isTrue();
        MasterList masterList = masterListOptional.get();
        assertThat(masterList.isLocked()).isTrue();
    }

    @Test
    public void find_whenThereIsARecentListUnlock_returnsAnUnlockedMasterList() throws Exception {
        String userEmail = "thatUserEmail";
        when(masterListDao.findByEmail(anyString())).thenReturn(
            MasterListEntity.builder()
                .email(userEmail)
                .todoEntities(emptyList())
                .lastUnlockedAt(Date.from(Instant.now().minusSeconds(500)))
                .build());

        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>("soUnique");
        Optional<MasterList> masterListOptional = masterListRepository.find(uniqueIdentifier);

        assertThat(masterListOptional.isPresent()).isTrue();
        MasterList masterList = masterListOptional.get();
        assertThat(masterList.isLocked()).isFalse();
    }

    @Test
    public void save_findsUserAndSavesRelationship() throws Exception {
        UserEntity userEntity = UserEntity.builder().id(123L).build();
        when(userDao.findByEmail(anyString())).thenReturn(userEntity);

        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>("someIdentifier");
        Date lastUnlockedAt = new Date();
        MasterList masterList = new MasterList(Clock.systemDefaultZone(),
            uniqueIdentifier,
            singletonList(new Todo("uuid1", "task1", "now", 1)),
            singletonList(new Todo("uuid2", "task2", "later", 2)),
            lastUnlockedAt);

        masterListRepository.save(masterList);

        verify(userDao).findByEmail("someIdentifier");
        ArgumentCaptor<MasterListEntity> argumentCaptor = ArgumentCaptor.forClass(MasterListEntity.class);
        verify(masterListDao).save(argumentCaptor.capture());
        MasterListEntity masterListEntity = argumentCaptor.getValue();
        assertThat(masterListEntity.id).isEqualTo(123L);
        assertThat(masterListEntity.email).isEqualTo("someIdentifier");
        assertThat(masterListEntity.todoEntities).contains(
            TodoEntity.builder()
                .userEntity(userEntity)
                .uuid("uuid1")
                .task("task1")
                .active(true)
                .position(1)
                .build(),
            TodoEntity.builder()
                .userEntity(userEntity)
                .uuid("uuid2")
                .task("task2")
                .active(false)
                .position(2)
                .build());
        assertThat(masterListEntity.lastUnlockedAt).isEqualTo(lastUnlockedAt);
    }

    @Test
    public void save_throwsAbnormalModelException_whenUserNotFound() throws Exception {
        when(userDao.findByEmail(anyString())).thenReturn(null);

        exception.expect(AbnormalModelException.class);

        MasterList masterList = new MasterList(Clock.systemDefaultZone(), new UniqueIdentifier<>("someIdentifier"), emptyList(), emptyList(), null);
        masterListRepository.save(masterList);
    }
}