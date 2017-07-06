package com.doerapispring.web;

import com.doerapispring.authentication.AuthenticatedAuthenticationToken;
import com.doerapispring.authentication.AuthenticatedUser;
import org.assertj.core.api.Assertions;
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

import static com.doerapispring.web.MockHateoasLinkGenerator.MOCK_BASE_URL;
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
        mockMvc.perform(post("/v1/list/unlock"))
                .andExpect(status().isAccepted());

        verify(mockListApiService).unlock(authenticatedUser);
    }

    @Test
    public void unlock_callsTodoService_returns202() throws Exception {
        ResponseEntity<ResourcesResponse> responseEntity = listsController.unlock(authenticatedUser);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getLinks()).contains(
                new Link(MOCK_BASE_URL + "/list/unlockTodos").withSelfRel(),
                new Link(MOCK_BASE_URL + "/list").withRel("list"));
    }

    @Test
    public void unlock_whenInvalidRequest_returns400BadRequest() throws Exception {
        doThrow(new InvalidRequestException()).when(mockListApiService).unlock(any());

        ResponseEntity<ResourcesResponse> responseEntity = listsController.unlock(authenticatedUser);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void show_mapping() throws Exception {
        when(mockListApiService.get(any())).thenReturn(new MasterListDTO("someName", "someDeferredName", false));

        mockMvc.perform(get("/v1/list"))
                .andExpect(status().isOk());

        verify(mockListApiService).get(authenticatedUser);
    }

    @Test
    public void show_includesLinks_byDefault() throws Exception {
        when(mockListApiService.get(any())).thenReturn(new MasterListDTO("someName", "someDeferredName", false));

        ResponseEntity<ListResponse> responseEntity = listsController.show(authenticatedUser);

        assertThat(responseEntity.getBody().getLinks()).contains(
                new Link(MOCK_BASE_URL + "/list").withSelfRel());
    }

    @Test
    public void show_returnsList() throws Exception {
        MasterListDTO masterListDTO = new MasterListDTO("someName", "someDeferredName", false);
        when(mockListApiService.get(any())).thenReturn(masterListDTO);

        ResponseEntity<ListResponse> responseEntity = listsController.show(authenticatedUser);

        assertThat(responseEntity.getBody().getMasterListDTO()).isEqualTo(masterListDTO);
    }

    @Test
    public void show_whenListIsNotFull_includesListLinks() throws Exception {
        when(mockListApiService.get(any())).thenReturn(new MasterListDTO("someName", "someDeferredName", false));

        ResponseEntity<ListResponse> responseEntity = listsController.show(authenticatedUser);

        Assertions.assertThat(responseEntity.getBody().getMasterListDTO().getLinks()).contains(
                new Link(MOCK_BASE_URL + "/list/createTodo").withRel("create"),
                new Link(MOCK_BASE_URL + "/list/pullTodos").withRel("pull"));
    }

    @Test
    public void show_whenListIsFull_doesNotIncludeListLinks() throws Exception {
        when(mockListApiService.get(any())).thenReturn(new MasterListDTO("someName", "someDeferredName", true));

        ResponseEntity<ListResponse> responseEntity = listsController.show(authenticatedUser);

        Assertions.assertThat(responseEntity.getBody().getMasterListDTO().getLinks()).isEmpty();
    }

    @Test
    public void show_whenInvalidRequest_throws400BadRequest() throws Exception {
        when(mockListApiService.get(any())).thenThrow(new InvalidRequestException());

        ResponseEntity<ListResponse> responseEntity = listsController.show(authenticatedUser);

        Assertions.assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}