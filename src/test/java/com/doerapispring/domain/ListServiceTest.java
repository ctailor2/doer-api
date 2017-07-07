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

import java.util.Collections;
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
    ArgumentCaptor<ListUnlock> listUnlockArgumentCaptor;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        listService = new ListService(mockMasterListRepository, mockListUnlockRepository);
    }

    @Test
    public void unlock_whenMasterListFound_addsListUnlockToRepository() throws Exception {
        MasterList mockMasterList = mock(MasterList.class);
        when(mockMasterListRepository.find(any())).thenReturn(Optional.of(mockMasterList));
        ListUnlock listUnlock = new ListUnlock();
        when(mockMasterList.unlock()).thenReturn(listUnlock);

        listService.unlock(new User(new UniqueIdentifier<>("testItUp")));

        verify(mockMasterList).unlock();
        verify(mockListUnlockRepository).add(mockMasterList, listUnlock);
    }

    @Test
    public void unlock_whenMasterListFound_whenRepositoryRejectsModels_refusesCreate() throws Exception {
        MasterList mockMasterList = mock(MasterList.class);
        when(mockMasterListRepository.find(any())).thenReturn(Optional.of(mockMasterList));
        ListUnlock listUnlock = new ListUnlock();
        when(mockMasterList.unlock()).thenReturn(listUnlock);
        doThrow(new AbnormalModelException()).when(mockListUnlockRepository).add(any(), any());

        exception.expect(OperationRefusedException.class);
        listService.unlock(new User(new UniqueIdentifier<>("testItUp")));
    }

    @Test
    public void unlock_whenMasterListFound_whenLockTimerNotExpired_refusesCreate() throws Exception {
        MasterList mockMasterList = mock(MasterList.class);
        when(mockMasterListRepository.find(any())).thenReturn(Optional.of(mockMasterList));
        when(mockMasterList.unlock()).thenThrow(new LockTimerNotExpiredException());

        exception.expect(OperationRefusedException.class);
        listService.unlock(new User(new UniqueIdentifier<>("testItUp")));
    }

    @Test
    public void unlock_whenMasterListNotFound_refusesCreate() throws Exception {
        when(mockMasterListRepository.find(any())).thenReturn(Optional.empty());

        exception.expect(OperationRefusedException.class);
        listService.unlock(new User(new UniqueIdentifier<>("testItUp")));
    }

    @Test
    public void get_whenMasterListFound_returnsMasterListFromRepository() throws Exception {
        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>("one@two.com");
        TodoList nowList = new TodoList(ScheduledFor.now, Collections.emptyList(), 2);
        TodoList laterList = new TodoList(ScheduledFor.later, Collections.emptyList(), -1);
        MasterList masterListFromRepository = new MasterList(uniqueIdentifier, nowList, laterList, Collections.emptyList());
        when(mockMasterListRepository.find(any())).thenReturn(Optional.of(masterListFromRepository));
        User user = new User(uniqueIdentifier);

        MasterList masterList = listService.get(user);

        assertThat(masterList).isEqualTo(masterListFromRepository);
    }

    @Test
    public void get_whenMasterListNotFound_refusesOperation() throws Exception {
        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>("one@two.com");
        when(mockMasterListRepository.find(any())).thenReturn(Optional.empty());

        exception.expect(OperationRefusedException.class);
        listService.get(new User(uniqueIdentifier));
    }
}