package com.doerapispring.domain;

import com.doerapispring.web.*;
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
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ListServiceTest {
    private ListService listService;

    @Mock
    private ObjectRepository<MasterList, String> mockMasterListRepository;

    @Mock
    private ObjectRepository<CompletedList, String> mockCompletedListRepository;

    @Rule
    public ExpectedException exception = ExpectedException.none();
    private MasterList masterList;
    private CompletedList completedList;
    private UniqueIdentifier<String> uniqueIdentifier;

    @Before
    public void setUp() throws Exception {
        listService = new ListService(mockMasterListRepository, mockCompletedListRepository);
        uniqueIdentifier = new UniqueIdentifier<>("userId");
        masterList = mock(MasterList.class);
        completedList = mock(CompletedList.class);
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
        MasterList masterList = new MasterList(
            Clock.systemDefaultZone(),
            new UniqueIdentifier<>("someIdentifier"),
            Date.from(Instant.now().minusMillis(1798766)),
            new ArrayList<>(),
            0
        );
        masterList.add("task");
        Todo todo = masterList.getTodos().get(0);
        masterList.addDeferred("deferredTask");
        Todo deferredTodo = masterList.getDeferredTodos().get(0);
        when(mockMasterListRepository.find(any())).thenReturn(Optional.of(masterList));
        User user = new User(uniqueIdentifier);

        MasterListDTO masterListDTO = listService.get(user);

        assertThat(masterListDTO).isNotNull();
        assertThat(masterListDTO.getTodos()).contains(new TodoDTO(todo.getLocalIdentifier(), todo.getTask()));
        assertThat(masterListDTO.getDeferredTodos()).contains(new TodoDTO(deferredTodo.getLocalIdentifier(), deferredTodo.getTask()));
        assertThat(masterListDTO.getName()).isEqualTo("now");
        assertThat(masterListDTO.getDeferredName()).isEqualTo("later");
        assertThat(masterListDTO.getUnlockDuration()).isCloseTo(1234L, within(100L));
        assertThat(masterListDTO.isFull()).isFalse();
        assertThat(masterListDTO.isAbleToBeUnlocked()).isFalse();
        assertThat(masterListDTO.isAbleToBeReplenished()).isTrue();
    }

    @Test
    public void get_whenMasterListNotFound_refusesOperation() throws Exception {
        when(mockMasterListRepository.find(any())).thenReturn(Optional.empty());

        exception.expect(InvalidRequestException.class);
        listService.get(new User(uniqueIdentifier));
    }

    @Test
    public void get_whenCompletedListFound_returnsCompletedListFromRepository() throws Exception {
        CompletedList completedList = new CompletedList(
            Clock.systemDefaultZone(),
            new UniqueIdentifier<>("someIdentifier"),
            new ArrayList<>());
        completedList.add("some task");
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