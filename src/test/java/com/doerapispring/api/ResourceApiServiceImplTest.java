package com.doerapispring.api;

import com.doerapispring.authentication.AuthenticatedUser;
import com.doerapispring.domain.*;
import com.doerapispring.web.InvalidRequestException;
import com.doerapispring.web.TodoResourcesDTO;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ResourceApiServiceImplTest {
    private ResourceApiServiceImpl resourceApiService;

    @Mock
    private TodoService mockTodoService;

    @Mock
    private ListService mockListService;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        resourceApiService = new ResourceApiServiceImpl(mockTodoService, mockListService);
    }

    @Test
    public void getTodoResources_callsTodoServiceForNowList_callsListService_whenNowListHasCapacity_whenListIsUnlocked() throws Exception {
        TodoList mockTodoList = mock(TodoList.class);
        when(mockTodoList.isFull()).thenReturn(true);
        when(mockTodoService.getSubList(any(), any())).thenReturn(mockTodoList);

        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>("someIdentifier");
        ListManager mockListManager = mock(ListManager.class);
        when(mockListManager.isLocked()).thenReturn(false);
        when(mockListService.getListManager(any())).thenReturn(mockListManager);

        TodoResourcesDTO todoResourcesDTO = resourceApiService.getTodoResources(new AuthenticatedUser("someIdentifier"));

        verify(mockTodoService).getSubList(new User(uniqueIdentifier), ScheduledFor.now);
        verify(mockListService).getListManager(new User(uniqueIdentifier));
        assertThat(todoResourcesDTO.doesNowListHaveCapacity()).isFalse();
        assertThat(todoResourcesDTO.isLaterListUnlocked()).isTrue();
    }

    @Test
    public void getTodoResources_whenTodoServiceRefusesOperation_throwsInvalidRequestException() throws Exception {
        when(mockTodoService.getSubList(any(), any())).thenThrow(new OperationRefusedException());

        exception.expect(InvalidRequestException.class);
        resourceApiService.getTodoResources(new AuthenticatedUser("someIdentifier"));
    }

    @Test
    public void getTodoResources_whenListServiceRefusesOperation_throwsInvalidRequestException() throws Exception {
        when(mockListService.getListManager(any())).thenThrow(new OperationRefusedException());

        exception.expect(InvalidRequestException.class);
        resourceApiService.getTodoResources(new AuthenticatedUser("someIdentifier"));
    }
}