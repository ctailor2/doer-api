package com.doerapispring.api;

import com.doerapispring.authentication.AuthenticatedUser;
import com.doerapispring.domain.*;
import com.doerapispring.web.InvalidRequestException;
import com.doerapispring.web.MasterListDTO;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class ListApiServiceImplTest {
    private ListApiServiceImpl listApiServiceImpl;

    private ListService mockListService;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        mockListService = mock(ListService.class);
        listApiServiceImpl = new ListApiServiceImpl(mockListService);
    }

    @Test
    public void unlock_callsListService() throws Exception {
        AuthenticatedUser authenticatedUser = new AuthenticatedUser("someIdentifier");
        listApiServiceImpl.unlock(authenticatedUser);

        verify(mockListService).unlock(new User(new UniqueIdentifier<>("someIdentifier")));
    }

    @Test
    public void unlock_whenOperationRefused_throwsInvalidRequest() throws Exception {
        doThrow(new OperationRefusedException()).when(mockListService).unlock(any());

        exception.expect(InvalidRequestException.class);
        listApiServiceImpl.unlock(new AuthenticatedUser("someIdentifier"));
    }

    @Test
    public void get_callsListService() throws Exception {
        when(mockListService.get(any()))
            .thenReturn(new MasterList(Clock.systemDefaultZone(), new UniqueIdentifier<>("someIdentifier"), null, new ArrayList<>(), 0));

        listApiServiceImpl.get(new AuthenticatedUser("someIdentifier"));

        verify(mockListService).get(new User(new UniqueIdentifier<>("someIdentifier")));
    }

    @Test
    public void get_callsListService_returnsMasterListDTO() throws Exception {
        MasterList masterList = new MasterList(
            Clock.systemDefaultZone(),
            new UniqueIdentifier<>("someIdentifier"),
            Date.from(Instant.now().minusMillis(1798766)),
            new ArrayList<>(),
            0
        );
        masterList.addDeferred("someTask");
        when(mockListService.get(any())).thenReturn(masterList);

        MasterListDTO masterListDTO = listApiServiceImpl.get(new AuthenticatedUser("someIdentifier"));

        verify(mockListService).get(new User(new UniqueIdentifier<>("someIdentifier")));
        assertThat(masterListDTO).isNotNull();
        assertThat(masterListDTO.getName()).isEqualTo("now");
        assertThat(masterListDTO.getDeferredName()).isEqualTo("later");
        assertThat(masterListDTO.getUnlockDuration()).isCloseTo(1234L, within(100L));
        assertThat(masterListDTO.isFull()).isFalse();
        assertThat(masterListDTO.isLocked()).isFalse();
        assertThat(masterListDTO.isAbleToBeUnlocked()).isFalse();
        assertThat(masterListDTO.isAbleToBeReplenished()).isTrue();
    }

    @Test
    public void get_whenOperationRefused_throwsInvalidRequest() throws Exception {
        when(mockListService.get(any())).thenThrow(new OperationRefusedException());

        exception.expect(InvalidRequestException.class);
        listApiServiceImpl.get(new AuthenticatedUser("someIdentifier"));
    }
}