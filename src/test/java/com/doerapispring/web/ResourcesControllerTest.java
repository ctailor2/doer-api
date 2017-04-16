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

import java.util.Collections;

import static com.doerapispring.web.MockHateoasLinkGenerator.MOCK_BASE_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class ResourcesControllerTest {
    private MockMvc mockMvc;
    private ResourcesController resourcesController;
    private AuthenticatedUser authenticatedUser;

    @Mock
    TodoApiService mockTodoApiService;

    @Before
    public void setUp() throws Exception {
        String identifier = "test@email.com";
        authenticatedUser = new AuthenticatedUser(identifier);
        SecurityContextHolder.getContext().setAuthentication(new AuthenticatedAuthenticationToken(authenticatedUser));
        resourcesController = new ResourcesController(new MockHateoasLinkGenerator(), mockTodoApiService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(resourcesController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
        when(mockTodoApiService.get(any())).thenReturn(new MasterListDTO(Collections.emptyList(), false));
    }

    @Test
    public void base_mapping() throws Exception {
        mockMvc.perform(get("/v1/resources/base"))
                .andExpect(status().isOk());
    }

    @Test
    public void base_includesLinks() throws Exception {
        ResponseEntity<ResourcesResponse> responseEntity = resourcesController.base();

        assertThat(responseEntity.getBody().getLinks()).contains(
                new Link(MOCK_BASE_URL + "/baseResources").withSelfRel(),
                new Link(MOCK_BASE_URL + "/login").withRel("login"),
                new Link(MOCK_BASE_URL + "/signup").withRel("signup"));
    }

    @Test
    public void root_mapping() throws Exception {
        mockMvc.perform(get("/v1/resources/root"))
                .andExpect(status().isOk());
    }

    @Test
    public void root_includesLinks() throws Exception {
        ResponseEntity<ResourcesResponse> responseEntity = resourcesController.root();

        assertThat(responseEntity.getBody().getLinks()).contains(
                new Link(MOCK_BASE_URL + "/rootResources").withSelfRel(),
                new Link(MOCK_BASE_URL + "/todoResources").withRel("todoResources"),
                new Link(MOCK_BASE_URL + "/historyResources").withRel("historyResources"));
    }

    @Test
    public void todo_mapping() throws Exception {
        mockMvc.perform(get("/v1/resources/todo"))
                .andExpect(status().isOk());
    }

    @Test
    public void todo_whenSchedulingForNowIsNotAllowed_includesLinks() throws Exception {
        ResponseEntity<ResourcesResponse> responseEntity = resourcesController.todo(authenticatedUser);

        verify(mockTodoApiService).get(authenticatedUser);
        assertThat(responseEntity.getBody().getLinks()).containsOnly(
                new Link(MOCK_BASE_URL + "/todoResources").withSelfRel(),
                new Link(MOCK_BASE_URL + "/todos?scheduling=now").withRel("nowTodos"),
                new Link(MOCK_BASE_URL + "/todos?scheduling=later").withRel("laterTodos"),
                new Link(MOCK_BASE_URL + "/createTodoForLater").withRel("todoLater"));
    }

    @Test
    public void todo_whenSchedulingForNowIsAllowed_includesLinks() throws Exception {
        when(mockTodoApiService.get(any())).thenReturn(new MasterListDTO(Collections.emptyList(), true));

        ResponseEntity<ResourcesResponse> responseEntity = resourcesController.todo(authenticatedUser);

        verify(mockTodoApiService).get(authenticatedUser);
        assertThat(responseEntity.getBody().getLinks()).contains(
                new Link(MOCK_BASE_URL + "/createTodoForNow").withRel("todoNow"),
                new Link(MOCK_BASE_URL + "/todos/pullTodos").withRel("pull"));
    }

    @Test
    public void todo_whenInvalidRequest_throws400BadRequest() throws Exception {
        when(mockTodoApiService.get(any())).thenThrow(new InvalidRequestException());

        ResponseEntity<ResourcesResponse> responseEntity = resourcesController.todo(authenticatedUser);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void history_mapping() throws Exception {
        mockMvc.perform(get("/v1/resources/history"))
                .andExpect(status().isOk());
    }

    @Test
    public void history_includesLinks() throws Exception {
        ResponseEntity<ResourcesResponse> responseEntity = resourcesController.history();

        assertThat(responseEntity.getBody().getLinks()).contains(
                new Link(MOCK_BASE_URL + "/historyResources").withSelfRel(),
                new Link(MOCK_BASE_URL + "/completedTodos").withRel("completedTodos"));
    }
}