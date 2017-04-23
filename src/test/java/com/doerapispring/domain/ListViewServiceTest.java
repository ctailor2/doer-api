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
public class ListViewServiceTest {
    private ListViewService listViewService;

    @Mock
    private AggregateRootRepository<ListViewManager, ListView, String> mockListViewRepository;

    @Captor
    ArgumentCaptor<ListView> listViewArgumentCaptor;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        listViewService = new ListViewService(mockListViewRepository);
    }

    @Test
    public void get_whenListViewManagerFound_returnsListViewManagerFromRepository() throws Exception {
        UniqueIdentifier uniqueIdentifier = new UniqueIdentifier<>("one@two.com");
        ListViewManager listViewManagerFromRepository = new ListViewManager(uniqueIdentifier, Collections.emptyList());
        when(mockListViewRepository.find(any())).thenReturn(Optional.of(listViewManagerFromRepository));

        ListViewManager listViewManager = listViewService.get(new User(uniqueIdentifier));

        verify(mockListViewRepository).find(uniqueIdentifier);
        Assertions.assertThat(listViewManager).isEqualTo(listViewManagerFromRepository);
    }

    @Test
    public void get_whenListViewManagerNotFound_refusesGet() throws Exception {
        when(mockListViewRepository.find(any())).thenReturn(Optional.empty());

        exception.expect(OperationRefusedException.class);
        listViewService.get(new User(new UniqueIdentifier<>("one@two.com")));
    }

    @Test
    public void create_whenListViewManagerFound_addsListViewToRepository() throws Exception {
        ListViewManager mockListViewManager = mock(ListViewManager.class);
        when(mockListViewRepository.find(any())).thenReturn(Optional.of(mockListViewManager));
        ListView listView = new ListView();
        when(mockListViewManager.recordView()).thenReturn(listView);

        listViewService.create(new User(new UniqueIdentifier<>("testItUp")));

        verify(mockListViewManager).recordView();
        verify(mockListViewRepository).add(mockListViewManager, listView);
    }

    @Test
    public void create_whenListViewManagerFound_whenRepositoryRejectsModels_refusesCreate() throws Exception {
        ListViewManager mockListViewManager = mock(ListViewManager.class);
        when(mockListViewRepository.find(any())).thenReturn(Optional.of(mockListViewManager));
        ListView listView = new ListView();
        when(mockListViewManager.recordView()).thenReturn(listView);
        doThrow(new AbnormalModelException()).when(mockListViewRepository).add(any(), any());

        exception.expect(OperationRefusedException.class);
        listViewService.create(new User(new UniqueIdentifier<>("testItUp")));
    }

    @Test
    public void create_whenListViewManagerFound_whenLockTimerNotExpired_refusesCreate() throws Exception {
        ListViewManager mockListViewManager = mock(ListViewManager.class);
        when(mockListViewRepository.find(any())).thenReturn(Optional.of(mockListViewManager));
        when(mockListViewManager.recordView()).thenThrow(new LockTimerNotExpiredException());

        exception.expect(OperationRefusedException.class);
        listViewService.create(new User(new UniqueIdentifier<>("testItUp")));
    }

    @Test
    public void create_whenListViewManagerNotFound_refusesCreate() throws Exception {
        when(mockListViewRepository.find(any())).thenReturn(Optional.empty());

        exception.expect(OperationRefusedException.class);
        listViewService.create(new User(new UniqueIdentifier<>("testItUp")));
    }
}