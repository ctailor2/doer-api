package com.doerapispring.storage;

import com.doerapispring.domain.AbnormalModelException;
import com.doerapispring.domain.ListManager;
import com.doerapispring.domain.ListUnlock;
import com.doerapispring.domain.UniqueIdentifier;
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

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class ListUnlockRepositoryTest {
    private ListUnlockRepository listUnlockRepository;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Mock
    ListUnlockDao mockListUnlockDao;

    @Mock
    UserDAO mockUserDao;

    @Captor
    ArgumentCaptor<ListUnlockEntity> listUnlockEntityArgumentCaptor;

    @Before
    public void setUp() throws Exception {
        listUnlockRepository = new ListUnlockRepository(mockListUnlockDao, mockUserDao);
    }

    @Test
    public void find_callsListUnlockDao() throws Exception {
        listUnlockRepository.find(new UniqueIdentifier<>("somethingSecret"));

        verify(mockListUnlockDao).findAllUserListUnlocks("somethingSecret");
    }

    @Test
    public void find_whenThereAreNoListUnlocks_returnsListManagerWithNoListUnlocks() throws Exception {
        when(mockListUnlockDao.findAllUserListUnlocks(any())).thenReturn(Collections.emptyList());

        UniqueIdentifier uniqueIdentifier = new UniqueIdentifier<>("soUnique");
        Optional<ListManager> listManagerOptional = listUnlockRepository.find(uniqueIdentifier);

        assertThat(listManagerOptional.isPresent()).isTrue();
        ListManager listManager = listManagerOptional.get();
        assertThat(listManager).isEqualTo(new ListManager(uniqueIdentifier, Collections.emptyList()));
    }

    @Test
    public void find_whenThereAreListUnlocks_returnsListManagerWithListUnlocks() throws Exception {
        List<ListUnlockEntity> listUnlockEntities = Collections.singletonList(
                ListUnlockEntity.builder()
                        .updatedAt(new Date(0L))
                        .build());
        when(mockListUnlockDao.findAllUserListUnlocks(any())).thenReturn(listUnlockEntities);

        UniqueIdentifier uniqueIdentifier = new UniqueIdentifier<>("soUnique");
        Optional<ListManager> listManagerOptional = listUnlockRepository.find(uniqueIdentifier);

        assertThat(listManagerOptional.isPresent()).isTrue();
        ListManager listManager = listManagerOptional.get();
        List<ListUnlock> listUnlocks = Collections.singletonList(new ListUnlock(new Date(0)));
        assertThat(listManager).isEqualTo(new ListManager(uniqueIdentifier, listUnlocks));
    }

    @Test
    public void add_findsUser_whenFound_savesRelationship_setsAuditingData() throws Exception {
        UserEntity userEntity = UserEntity.builder().build();
        when(mockUserDao.findByEmail(any())).thenReturn(userEntity);

        ListUnlock listUnlock = new ListUnlock();
        ListManager listManager = new ListManager(new UniqueIdentifier("listUserIdentifier"), Collections.emptyList());
        listUnlockRepository.add(listManager, listUnlock);

        verify(mockUserDao).findByEmail("listUserIdentifier");
        verify(mockListUnlockDao).save(listUnlockEntityArgumentCaptor.capture());
        ListUnlockEntity todoEntity = listUnlockEntityArgumentCaptor.getValue();
        assertThat(todoEntity).isNotNull();
        assertThat(todoEntity.userEntity).isEqualTo(userEntity);
        assertThat(todoEntity.createdAt).isToday();
        assertThat(todoEntity.updatedAt).isToday();
    }

    @Test
    public void add_findsUser_whenNotFound_throwsAbnormalModelException() throws Exception {
        when(mockUserDao.findByEmail(any())).thenReturn(null);

        exception.expect(AbnormalModelException.class);
        ListUnlock listUnlock = new ListUnlock();
        ListManager listManager = new ListManager(new UniqueIdentifier("nonExistentUser"), Collections.emptyList());
        listUnlockRepository.add(listManager, listUnlock);

        verify(mockUserDao).findByEmail("nonExistentUser");
    }
}