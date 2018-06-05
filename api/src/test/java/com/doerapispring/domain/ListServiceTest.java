package com.doerapispring.domain;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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

    @Rule
    public ExpectedException exception = ExpectedException.none();
    private MasterList masterList;
    private UniqueIdentifier<String> uniqueIdentifier;

    @Before
    public void setUp() throws Exception {
        listService = new ListService(mockMasterListRepository, mockListUnlockRepository);
        uniqueIdentifier = new UniqueIdentifier<>("userId");
        masterList = mock(MasterList.class);
        when(mockMasterListRepository.find(any())).thenReturn(Optional.of(masterList));
    }

    @Test
    public void unlock_whenMasterListFound_unlocksMasterList_andSavesIt() throws Exception {
        ListUnlock listUnlock = new ListUnlock(new Date());
        when(masterList.unlock()).thenReturn(listUnlock);

        listService.unlock(new User(uniqueIdentifier));

        verify(masterList).unlock();
        verify(mockListUnlockRepository).add(masterList, listUnlock);
    }

    @Test
    public void unlock_whenMasterListFound_whenRepositoryRejectsModels_refusesOperation() throws Exception {
        doThrow(new AbnormalModelException()).when(mockListUnlockRepository).add(any(), any());

        exception.expect(OperationRefusedException.class);
        listService.unlock(new User(uniqueIdentifier));
    }

    @Test
    public void unlock_whenMasterListFound_whenLockTimerNotExpired_refusesOperation() throws Exception {
        doThrow(new LockTimerNotExpiredException()).when(masterList).unlock();

        exception.expect(OperationRefusedException.class);
        listService.unlock(new User(uniqueIdentifier));
    }

    @Test
    public void unlock_whenMasterListNotFound_refusesOperation() throws Exception {
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