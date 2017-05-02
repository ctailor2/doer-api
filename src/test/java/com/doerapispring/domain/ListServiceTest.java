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
    private AggregateRootRepository<ListManager, ListUnlock, String> mockListViewRepository;

    @Captor
    ArgumentCaptor<ListUnlock> listViewArgumentCaptor;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        listService = new ListService(mockListViewRepository);
    }

    @Test
    public void get_whenListViewManagerFound_returnsListViewManagerFromRepository() throws Exception {
        UniqueIdentifier uniqueIdentifier = new UniqueIdentifier<>("one@two.com");
        ListManager listViewManagerFromRepository = new ListManager(uniqueIdentifier, Collections.emptyList());
        when(mockListViewRepository.find(any())).thenReturn(Optional.of(listViewManagerFromRepository));

        ListManager listViewManager = listService.get(new User(uniqueIdentifier));

        verify(mockListViewRepository).find(uniqueIdentifier);
        Assertions.assertThat(listViewManager).isEqualTo(listViewManagerFromRepository);
    }

    @Test
    public void get_whenListViewManagerNotFound_refusesGet() throws Exception {
        when(mockListViewRepository.find(any())).thenReturn(Optional.empty());

        exception.expect(OperationRefusedException.class);
        listService.get(new User(new UniqueIdentifier<>("one@two.com")));
    }

    @Test
    public void create_whenListViewManagerFound_addsListViewToRepository() throws Exception {
        ListManager mockListViewManager = mock(ListManager.class);
        when(mockListViewRepository.find(any())).thenReturn(Optional.of(mockListViewManager));
        ListUnlock listUnlock = new ListUnlock();
        when(mockListViewManager.unlock()).thenReturn(listUnlock);

        listService.unlock(new User(new UniqueIdentifier<>("testItUp")));

        verify(mockListViewManager).unlock();
        verify(mockListViewRepository).add(mockListViewManager, listUnlock);
    }

    @Test
    public void create_whenListViewManagerFound_whenRepositoryRejectsModels_refusesCreate() throws Exception {
        ListManager mockListViewManager = mock(ListManager.class);
        when(mockListViewRepository.find(any())).thenReturn(Optional.of(mockListViewManager));
        ListUnlock listUnlock = new ListUnlock();
        when(mockListViewManager.unlock()).thenReturn(listUnlock);
        doThrow(new AbnormalModelException()).when(mockListViewRepository).add(any(), any());

        exception.expect(OperationRefusedException.class);
        listService.unlock(new User(new UniqueIdentifier<>("testItUp")));
    }

    @Test
    public void create_whenListViewManagerFound_whenLockTimerNotExpired_refusesCreate() throws Exception {
        ListManager mockListViewManager = mock(ListManager.class);
        when(mockListViewRepository.find(any())).thenReturn(Optional.of(mockListViewManager));
        when(mockListViewManager.unlock()).thenThrow(new LockTimerNotExpiredException());

        exception.expect(OperationRefusedException.class);
        listService.unlock(new User(new UniqueIdentifier<>("testItUp")));
    }

    @Test
    public void create_whenListViewManagerNotFound_refusesCreate() throws Exception {
        when(mockListViewRepository.find(any())).thenReturn(Optional.empty());

        exception.expect(OperationRefusedException.class);
        listService.unlock(new User(new UniqueIdentifier<>("testItUp")));
    }
}