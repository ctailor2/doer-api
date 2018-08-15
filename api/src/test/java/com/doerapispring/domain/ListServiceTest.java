package com.doerapispring.domain;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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

        MasterList actualMasterList = listService.get(user);

        assertThat(actualMasterList).isEqualTo(masterList);
    }

    @Test
    public void get_whenMasterListNotFound_refusesOperation() throws Exception {
        when(mockMasterListRepository.find(any())).thenReturn(Optional.empty());

        exception.expect(OperationRefusedException.class);
        listService.get(new User(uniqueIdentifier));
    }

    @Test
    public void get_whenCompletedListFound_returnsCompletedListFromRepository() throws Exception {
        when(mockCompletedListRepository.find(any())).thenReturn(Optional.of(completedList));
        User user = new User(uniqueIdentifier);

        CompletedList actualCompletedList = listService.getCompleted(user);

        assertThat(actualCompletedList).isEqualTo(completedList);
    }

    @Test
    public void get_whenCompletedListNotFound_refusesOperation() throws Exception {
        when(mockCompletedListRepository.find(any())).thenReturn(Optional.empty());

        exception.expect(OperationRefusedException.class);
        listService.getCompleted(new User(uniqueIdentifier));
    }
}