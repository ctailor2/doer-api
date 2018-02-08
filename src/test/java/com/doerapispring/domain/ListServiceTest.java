package com.doerapispring.domain;

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
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ListServiceTest {
    private ListService listService;

    @Mock
    private ObjectRepository<MasterList, String> mockMasterListRepository;

    @Mock
    private AggregateRootRepository<MasterList, ListUnlock> mockListUnlockRepository;

    @Captor
    private ArgumentCaptor<ListUnlock> listUnlockArgumentCaptor;

    @Rule
    public ExpectedException exception = ExpectedException.none();
    private MasterList masterList;
    private UniqueIdentifier<String> uniqueIdentifier;

    @Before
    public void setUp() throws Exception {
        listService = new ListService(mockMasterListRepository, mockListUnlockRepository);
        uniqueIdentifier = new UniqueIdentifier<>("userId");
        masterList = new MasterList(Clock.systemDefaultZone(), uniqueIdentifier, new ArrayList<>());
        when(mockMasterListRepository.find(any())).thenReturn(Optional.of(masterList));
    }

    @Test
    public void unlock_whenMasterListFound_addsListUnlockToRepository() throws Exception {
        listService.unlock(new User(uniqueIdentifier));

        verify(mockListUnlockRepository).add(eq(masterList), listUnlockArgumentCaptor.capture());

        ListUnlock listUnlock = listUnlockArgumentCaptor.getValue();
        assertThat(listUnlock.getCreatedAt()).isAfter(Date.from(Instant.now().minusMillis(100)));
    }

    @Test
    public void unlock_whenMasterListFound_whenRepositoryRejectsModels_refusesCreate() throws Exception {
        doThrow(new AbnormalModelException()).when(mockListUnlockRepository).add(any(), any());

        exception.expect(OperationRefusedException.class);
        listService.unlock(new User(uniqueIdentifier));
    }

    @Test
    public void unlock_whenMasterListFound_whenLockTimerNotExpired_refusesCreate() throws Exception {
        masterList.unlock();

        exception.expect(OperationRefusedException.class);
        listService.unlock(new User(uniqueIdentifier));
    }

    @Test
    public void unlock_whenMasterListNotFound_refusesCreate() throws Exception {
        when(mockMasterListRepository.find(any())).thenReturn(Optional.empty());

        exception.expect(OperationRefusedException.class);
        listService.unlock(new User(uniqueIdentifier));
    }

    @Test
    public void get_whenMasterListFound_returnsMasterListFromRepository() throws Exception {
        when(mockMasterListRepository.find(any())).thenReturn(Optional.of(masterList));
        User user = new User(uniqueIdentifier);

        MasterList masterList = listService.get(user);

        assertThat(masterList).isEqualTo(masterList);
    }

    @Test
    public void get_whenMasterListNotFound_refusesOperation() throws Exception {
        when(mockMasterListRepository.find(any())).thenReturn(Optional.empty());

        exception.expect(OperationRefusedException.class);
        listService.get(new User(uniqueIdentifier));
    }
}