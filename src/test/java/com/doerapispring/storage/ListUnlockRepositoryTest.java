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
    public void add_findsUser_whenFound_savesRelationship_setsAuditingData() throws Exception {
        UserEntity userEntity = UserEntity.builder().build();
        when(mockUserDao.findByEmail(any())).thenReturn(userEntity);

        MasterList masterList = new MasterList(
            Clock.systemDefaultZone(),
            new UniqueIdentifier<>("listUserIdentifier"),
            new TodoList(ScheduledFor.now, Collections.emptyList(), 0),
            new TodoList(ScheduledFor.later, Collections.emptyList(), 0), Collections.emptyList());
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
            new TodoList(ScheduledFor.now, Collections.emptyList(), 0),
            new TodoList(ScheduledFor.later, Collections.emptyList(), 0), Collections.emptyList());
        listUnlockRepository.add(masterList, new ListUnlock());

        verify(mockUserDao).findByEmail("nonExistentUser");
    }
}