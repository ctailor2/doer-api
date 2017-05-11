package com.doerapispring.web;

import com.doerapispring.authentication.AuthenticatedAuthenticationToken;
import com.doerapispring.authentication.AuthenticatedUser;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static com.doerapispring.web.MockHateoasLinkGenerator.MOCK_BASE_URL;
import static java.util.Arrays.asList;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class ListsControllerTest {
    private ListsController listsController;

    @Mock
    private ListApiService mockListApiService;

    private MockMvc mockMvc;
    private AuthenticatedUser authenticatedUser;

    @Before
    public void setUp() throws Exception {
        String identifier = "test@email.com";
        authenticatedUser = new AuthenticatedUser(identifier);
        SecurityContextHolder.getContext().setAuthentication(new AuthenticatedAuthenticationToken(authenticatedUser));
        listsController = new ListsController(new MockHateoasLinkGenerator(), mockListApiService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(listsController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @Test
    public void unlock_mapping() throws Exception {
        mockMvc.perform(post("/v1/lists/unlock"))
                .andExpect(status().isAccepted());

        verify(mockListApiService).unlock(authenticatedUser);
    }

    @Test
    public void unlock_callsTodoService_returns202() throws Exception {
        ResponseEntity<ResourcesResponse> responseEntity = listsController.unlock(authenticatedUser);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getLinks()).contains(
                new Link(MOCK_BASE_URL + "/todos/unlockTodos").withSelfRel(),
                new Link(MOCK_BASE_URL + "/todos?scheduling=later").withRel("laterTodos"));
    }

    @Test
    public void unlock_whenInvalidRequest_returns400BadRequest() throws Exception {
        doThrow(new InvalidRequestException()).when(mockListApiService).unlock(any());

        ResponseEntity<ResourcesResponse> responseEntity = listsController.unlock(authenticatedUser);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void index_mapping() throws Exception {
        mockMvc.perform(get("/v1/lists"))
                .andExpect(status().isOk());

        verify(mockListApiService).getAll(authenticatedUser);
    }

    @Test
    public void index_callsListService_includesLinksByDefault() throws Exception {
        ResponseEntity<ListsResponse> responseEntity = listsController.index(authenticatedUser);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getBody().getLinks())
                .contains(new Link(MOCK_BASE_URL + "/lists").withSelfRel());
    }

    @Test
    public void index_callsListService_byDefault_includesLinksForEachList() throws Exception {
        when(mockListApiService.getAll(any()))
                .thenReturn(asList(new ListDTO("someName"), new ListDTO("someOtherName")));

        ResponseEntity<ListsResponse> responseEntity = listsController.index(authenticatedUser);

        List<ListDTO> listDTOs = responseEntity.getBody().getListDTOs();
        assertThat(listDTOs.size()).isEqualTo(2);
        ListDTO firstListDTO = listDTOs.get(0);
        assertThat(firstListDTO.getName()).isEqualTo("someName");
        assertThat(firstListDTO.getLinks()).containsOnly(
                new Link(MOCK_BASE_URL + "/lists/someName").withRel("list"));
        ListDTO secondListDTO = listDTOs.get(1);
        assertThat(secondListDTO.getName()).isEqualTo("someOtherName");
        assertThat(secondListDTO.getLinks()).containsOnly(
                new Link(MOCK_BASE_URL + "/lists/someOtherName").withRel("list"));
    }

    @Test
    public void index_whenInvalidRequest_returns400BadRequest() throws Exception {
        doThrow(new InvalidRequestException()).when(mockListApiService).getAll(any());

        ResponseEntity<ListsResponse> responseEntity = listsController.index(authenticatedUser);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}