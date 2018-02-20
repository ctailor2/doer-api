package com.doerapispring.storage;

import com.doerapispring.domain.AbnormalModelException;
import com.doerapispring.domain.ListUnlock;
import com.doerapispring.domain.MasterList;
import com.doerapispring.domain.UniqueIdentifier;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;

import java.time.Clock;
import java.util.Collections;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ListUnlockRepositoryTest {
    private ListUnlockRepository listUnlockRepository;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private ListUnlockDao mockListUnlockDao;

    private UserDAO mockUserDao;

    private final ArgumentCaptor<ListUnlockEntity> listUnlockEntityArgumentCaptor = ArgumentCaptor.forClass(ListUnlockEntity.class);

    @Before
    public void setUp() throws Exception {
        mockListUnlockDao = mock(ListUnlockDao.class);
        mockUserDao = mock(UserDAO.class);
        listUnlockRepository = new ListUnlockRepository(mockListUnlockDao, mockUserDao);
    }

    @Test
    public void add_findsUser_whenFound_savesRelationship_setsAuditingData() throws Exception {
        UserEntity userEntity = UserEntity.builder().build();
        when(mockUserDao.findByEmail(any())).thenReturn(userEntity);

        MasterList masterList = new MasterList(
            Clock.systemDefaultZone(),
            new UniqueIdentifier<>("listUserIdentifier"),
            Collections.emptyList());
        listUnlockRepository.add(masterList, new ListUnlock());

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
        MasterList masterList = new MasterList(
            Clock.systemDefaultZone(),
            new UniqueIdentifier<>("listUserIdentifier"),
            Collections.emptyList());
        listUnlockRepository.add(masterList, new ListUnlock());

        verify(mockUserDao).findByEmail("nonExistentUser");
    }
}