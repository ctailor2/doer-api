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
    ResourceApiService mockResourceApiService;

    @Before
    public void setUp() throws Exception {
        String identifier = "test@email.com";
        authenticatedUser = new AuthenticatedUser(identifier);
        SecurityContextHolder.getContext().setAuthentication(new AuthenticatedAuthenticationToken(authenticatedUser));
        resourcesController = new ResourcesController(new MockHateoasLinkGenerator(), mockResourceApiService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(resourcesController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
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
        when(mockResourceApiService.getTodoResources(any())).thenReturn(new TodoResourcesDTO(true, false));

        mockMvc.perform(get("/v1/resources/todo"))
                .andExpect(status().isOk());

        verify(mockResourceApiService).getTodoResources(authenticatedUser);
    }

    @Test
    public void todo_whenLaterListIsUnlocked_includesLinks() throws Exception {
        when(mockResourceApiService.getTodoResources(any())).thenReturn(new TodoResourcesDTO(true, false));

        ResponseEntity<ResourcesResponse> responseEntity = resourcesController.todo(authenticatedUser);

        assertThat(responseEntity.getBody().getLinks()).containsOnly(
                new Link(MOCK_BASE_URL + "/todoResources").withSelfRel(),
                new Link(MOCK_BASE_URL + "/todos?scheduling=now").withRel("nowTodos"),
                new Link(MOCK_BASE_URL + "/todos?scheduling=later").withRel("laterTodos"));
    }

    @Test
    public void todo_whenLaterListIsLocked_excludesLinks() throws Exception {
        when(mockResourceApiService.getTodoResources(any())).thenReturn(new TodoResourcesDTO(true, true));

        ResponseEntity<ResourcesResponse> responseEntity = resourcesController.todo(authenticatedUser);

        assertThat(responseEntity.getBody().getLinks()).doesNotContain(
                new Link(MOCK_BASE_URL + "/todos?scheduling=later").withRel("laterTodos"));
    }

    @Test
    public void todo_whenInvalidRequest_throws400BadRequest() throws Exception {
        when(mockResourceApiService.getTodoResources(any())).thenThrow(new InvalidRequestException());

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