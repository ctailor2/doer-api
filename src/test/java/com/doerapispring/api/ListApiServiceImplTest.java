package com.doerapispring.api;

import com.doerapispring.authentication.AuthenticatedUser;
import com.doerapispring.domain.*;
import com.doerapispring.web.InvalidRequestException;
import com.doerapispring.web.ListDTO;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class ListApiServiceImplTest {
    private ListApiServiceImpl listApiServiceImpl;

    @Mock
    private ListService mockListService;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        listApiServiceImpl = new ListApiServiceImpl(mockListService);
    }

    @Test
    public void unlock_callsListService() throws Exception {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser("someIdentifier");
        listApiServiceImpl.unlock(authenticatedUser);

        verify(mockListService).unlock(new User(new UniqueIdentifier("someIdentifier")));
    }

    @Test
    public void unlock_whenOperationRefused_throwsInvalidRequest() throws Exception {
        doThrow(new OperationRefusedException()).when(mockListService).unlock(any());

        exception.expect(InvalidRequestException.class);
        listApiServiceImpl.unlock(new AuthenticatedUser("someIdentifier"));
    }

    @Test
    public void getAll_callsListService() throws Exception {
        listApiServiceImpl.getAll(new AuthenticatedUser("someIdentifier"));

        verify(mockListService).getAll();
    }

    @Test
    public void getAll_whenListServiceReturnsLists_returnsMatchingListDTOs() throws Exception {
        when(mockListService.getAll()).thenReturn(asList(
                new BasicTodoList("someName"),
                new BasicTodoList("someOtherName")));

        List<ListDTO> listDTOs = listApiServiceImpl.getAll(new AuthenticatedUser("someIdentifier"));

        assertThat(listDTOs).isEqualTo(asList(
                new ListDTO("someName"),
                new ListDTO("someOtherName")));
    }

    @Test
    public void getAll_whenOperationRefused_throwsInvalidRequest() throws Exception {
        when(mockListService.getAll()).thenThrow(new OperationRefusedException());

        exception.expect(InvalidRequestException.class);
        listApiServiceImpl.getAll(new AuthenticatedUser("someIdentifier"));
    }
}