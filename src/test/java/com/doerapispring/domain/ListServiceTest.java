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
import java.util.List;
import java.util.Optional;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ListServiceTest {
    private ListService listService;

    @Mock
    private AggregateRootRepository<ListManager, ListUnlock, String> mockListUnlockRepository;

    @Mock
    private AggregateRootRepository<MasterList, Todo, String> mockTodoListRepository;

    @Captor
    ArgumentCaptor<ListUnlock> listUnlockArgumentCaptor;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        listService = new ListService(mockListUnlockRepository, mockTodoListRepository);
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

    @Test
    public void getAll_returnsNowAndLaterLists() throws Exception {
        List<BasicTodoList> basicTodoLists = listService.getAll();

        assertThat(basicTodoLists).isEqualTo(asList(
                new BasicTodoList("now"),
                new BasicTodoList("later")));
    }

    @Test
    public void get_whenMasterListFound_whenListWithNameExists_returnsMatchingList() throws Exception {
        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>("one@two.com");
        TodoList nowList = new TodoList(ScheduledFor.now, Collections.emptyList(), 2);
        TodoList laterList = new TodoList(ScheduledFor.later, Collections.emptyList(), -1);
        MasterList masterListFromRepository = new MasterList(uniqueIdentifier, nowList, laterList);
        when(mockTodoListRepository.find(any())).thenReturn(Optional.of(masterListFromRepository));
        User user = new User(uniqueIdentifier);

        TodoList todoList = listService.get(user, "now");

        assertThat(todoList).isEqualTo(nowList);
    }

    @Test
    public void get_whenMasterListFound_whenListWithNameDoesNotExist_refusesOperation() throws Exception {
        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>("one@two.com");
        TodoList nowList = new TodoList(ScheduledFor.now, Collections.emptyList(), 2);
        TodoList laterList = new TodoList(ScheduledFor.later, Collections.emptyList(), -1);
        MasterList masterListFromRepository = new MasterList(uniqueIdentifier, nowList, laterList);
        when(mockTodoListRepository.find(any())).thenReturn(Optional.of(masterListFromRepository));
        User user = new User(uniqueIdentifier);

        exception.expect(OperationRefusedException.class);
        listService.get(user, "notThere");
    }

    @Test
    public void get_whenMasterListNotFound_refusesOperation() throws Exception {
        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>("one@two.com");
        when(mockTodoListRepository.find(any())).thenReturn(Optional.empty());

        exception.expect(OperationRefusedException.class);
        listService.get(new User(uniqueIdentifier), "now");
    }
}