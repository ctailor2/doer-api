package com.doerapispring.domain;

import org.fest.assertions.api.Assertions;
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

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ListServiceTest {
    private ListService listService;

    @Mock
    private AggregateRootRepository<ListManager, ListUnlock, String> mockListUnlockRepository;

    @Captor
    ArgumentCaptor<ListUnlock> listUnlockArgumentCaptor;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        listService = new ListService(mockListUnlockRepository);
    }

    @Test
    public void get_whenListManagerFound_returnsListManagerFromRepository() throws Exception {
        UniqueIdentifier uniqueIdentifier = new UniqueIdentifier<>("one@two.com");
        ListManager listUnlockManagerFromRepository = new ListManager(uniqueIdentifier, Collections.emptyList());
        when(mockListUnlockRepository.find(any())).thenReturn(Optional.of(listUnlockManagerFromRepository));

        ListManager listManager = listService.get(new User(uniqueIdentifier));

        verify(mockListUnlockRepository).find(uniqueIdentifier);
        Assertions.assertThat(listManager).isEqualTo(listUnlockManagerFromRepository);
    }

    @Test
    public void get_whenListManagerNotFound_refusesGet() throws Exception {
        when(mockListUnlockRepository.find(any())).thenReturn(Optional.empty());

        exception.expect(OperationRefusedException.class);
        listService.get(new User(new UniqueIdentifier<>("one@two.com")));
    }

    @Test
    public void create_whenListManagerFound_addsListUnlockToRepository() throws Exception {
        ListManager mockListManager = mock(ListManager.class);
        when(mockListUnlockRepository.find(any())).thenReturn(Optional.of(mockListManager));
        ListUnlock listUnlock = new ListUnlock();
        when(mockListManager.unlock()).thenReturn(listUnlock);

        listService.unlock(new User(new UniqueIdentifier<>("testItUp")));

        verify(mockListManager).unlock();
        verify(mockListUnlockRepository).add(mockListManager, listUnlock);
    }

    @Test
    public void create_whenListManagerFound_whenRepositoryRejectsModels_refusesCreate() throws Exception {
        ListManager mockListManager = mock(ListManager.class);
        when(mockListUnlockRepository.find(any())).thenReturn(Optional.of(mockListManager));
        ListUnlock listUnlock = new ListUnlock();
        when(mockListManager.unlock()).thenReturn(listUnlock);
        doThrow(new AbnormalModelException()).when(mockListUnlockRepository).add(any(), any());

        exception.expect(OperationRefusedException.class);
        listService.unlock(new User(new UniqueIdentifier<>("testItUp")));
    }

    @Test
    public void create_whenListManagerFound_whenLockTimerNotExpired_refusesCreate() throws Exception {
        ListManager mockListManager = mock(ListManager.class);
        when(mockListUnlockRepository.find(any())).thenReturn(Optional.of(mockListManager));
        when(mockListManager.unlock()).thenThrow(new LockTimerNotExpiredException());

        exception.expect(OperationRefusedException.class);
        listService.unlock(new User(new UniqueIdentifier<>("testItUp")));
    }

    @Test
    public void create_whenListManagerNotFound_refusesCreate() throws Exception {
        when(mockListUnlockRepository.find(any())).thenReturn(Optional.empty());

        exception.expect(OperationRefusedException.class);
        listService.unlock(new User(new UniqueIdentifier<>("testItUp")));
    }
}