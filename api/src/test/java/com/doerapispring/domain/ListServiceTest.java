package com.doerapispring.domain;

import com.doerapispring.web.CompletedListDTO;
import com.doerapispring.web.CompletedTodoDTO;
import com.doerapispring.web.InvalidRequestException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
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
    private ObjectRepository<CompletedList, String> mockCompletedListRepository;

    @Mock
    private ObjectRepository<ReadOnlyMasterList, String> mockReadOnlyMasterListRepository;

    @Rule
    public ExpectedException exception = ExpectedException.none();
    private MasterList masterList;
    private UniqueIdentifier<String> uniqueIdentifier;

    @Before
    public void setUp() throws Exception {
        listService = new ListService(mockMasterListRepository, mockCompletedListRepository, mockReadOnlyMasterListRepository);
        uniqueIdentifier = new UniqueIdentifier<>("userId");
        masterList = mock(MasterList.class);
        when(mockMasterListRepository.find(any())).thenReturn(Optional.of(masterList));
    }

    @Test
    public void unlock_whenMasterListFound_unlocksMasterList_andSavesIt() throws Exception {
        listService.unlock(new User(uniqueIdentifier));

        verify(masterList).unlock();
        verify(mockMasterListRepository).save(masterList);
    }

    @Test
    public void unlock_whenMasterListFound_whenRepositoryRejectsModels_refusesOperation() throws Exception {
        doThrow(new AbnormalModelException()).when(mockMasterListRepository).save(any());

        exception.expect(InvalidRequestException.class);
        listService.unlock(new User(uniqueIdentifier));
    }

    @Test
    public void unlock_whenMasterListFound_whenLockTimerNotExpired_refusesOperation() throws Exception {
        doThrow(new LockTimerNotExpiredException()).when(masterList).unlock();

        exception.expect(InvalidRequestException.class);
        listService.unlock(new User(uniqueIdentifier));
    }

    @Test
    public void unlock_whenMasterListNotFound_refusesOperation() throws Exception {
        when(mockMasterListRepository.find(any())).thenReturn(Optional.empty());

        exception.expect(InvalidRequestException.class);
        listService.unlock(new User(uniqueIdentifier));
    }

    @Test
    public void get_whenMasterListFound_returnsMasterListFromRepository() throws Exception {
        ReadOnlyMasterList readOnlyMasterList = new ReadOnlyMasterList(
            Clock.systemDefaultZone(),
            new UniqueIdentifier<>("someIdentifier"),
            Date.from(Instant.now().minusMillis(1798766)),
            new ArrayList<>(),
            0
        );
        when(mockReadOnlyMasterListRepository.find(any())).thenReturn(Optional.of(readOnlyMasterList));
        User user = new User(uniqueIdentifier);

        ReadOnlyMasterList actual = listService.get(user);

        assertThat(actual).isEqualTo(readOnlyMasterList);
    }

    @Test
    public void get_whenMasterListNotFound_refusesOperation() throws Exception {
        when(mockReadOnlyMasterListRepository.find(any())).thenReturn(Optional.empty());

        exception.expect(InvalidRequestException.class);
        listService.get(new User(uniqueIdentifier));
    }

    @Test
    public void get_whenCompletedListFound_returnsCompletedListFromRepository() throws Exception {
        CompletedList completedList = new CompletedList(
            Clock.systemDefaultZone(),
            new UniqueIdentifier<>("someIdentifier"),
            new ArrayList<>());
        completedList.add(null, "some task");
        CompletedTodo completedTodo = completedList.getTodos().get(0);
        when(mockCompletedListRepository.find(any())).thenReturn(Optional.of(completedList));
        User user = new User(uniqueIdentifier);

        CompletedListDTO completedListDTO = listService.getCompleted(user);

        assertThat(completedListDTO.getTodos()).contains(
            new CompletedTodoDTO(completedTodo.getTask(), completedTodo.getCompletedAt()));
    }

    @Test
    public void get_whenCompletedListNotFound_refusesOperation() throws Exception {
        when(mockCompletedListRepository.find(any())).thenReturn(Optional.empty());

        exception.expect(InvalidRequestException.class);
        listService.getCompleted(new User(uniqueIdentifier));
    }
}