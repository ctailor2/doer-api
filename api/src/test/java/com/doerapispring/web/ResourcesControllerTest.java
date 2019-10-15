package com.doerapispring.web;

import com.doerapispring.authentication.AuthenticatedAuthenticationToken;
import com.doerapispring.authentication.AuthenticatedUser;
import com.doerapispring.domain.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static com.doerapispring.web.MockHateoasLinkGenerator.MOCK_BASE_URL;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ResourcesControllerTest {
    private MockMvc mockMvc;
    private ResourcesController resourcesController;
    private AuthenticatedUser authenticatedUser;
    private ListApplicationService listApplicationService;
    private User user;
    private ListId defaultListId;

    @Before
    public void setUp() throws Exception {
        String identifier = "test@email.com";
        authenticatedUser = mock(AuthenticatedUser.class);
        user = new User(new UserId(identifier));
        when(authenticatedUser.getUser()).thenReturn(user);
        SecurityContextHolder.getContext().setAuthentication(new AuthenticatedAuthenticationToken(authenticatedUser));
        listApplicationService = mock(ListApplicationService.class);
        resourcesController = new ResourcesController(new MockHateoasLinkGenerator(), listApplicationService);
        mockMvc = MockMvcBuilders
            .standaloneSetup(resourcesController)
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .build();
        defaultListId = new ListId("someListId");
        when(listApplicationService.getDefault(any())).thenReturn(new TodoListReadModel(null, null, null, emptyList(), null, defaultListId, user.getUserId()));
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

        verify(listApplicationService).getDefault(user);
    }

    @Test
    public void todo_includesLinksByDefault() throws Exception {
        ResponseEntity<ResourcesResponse> responseEntity = resourcesController.todo(authenticatedUser);

        assertThat(responseEntity.getBody().getLinks()).containsOnly(
            new Link(MOCK_BASE_URL + "/todoResources").withSelfRel(),
            new Link(MOCK_BASE_URL + "/lists/" + defaultListId.get()).withRel("list"));
    }

    @Test
    public void history_mapping() throws Exception {
        mockMvc.perform(get("/v1/resources/history"))
            .andExpect(status().isOk());
    }

    @Test
    public void history_includesLinks() throws Exception {
        ResponseEntity<ResourcesResponse> responseEntity = resourcesController.history(authenticatedUser);

        assertThat(responseEntity.getBody().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/historyResources").withSelfRel(),
            new Link(MOCK_BASE_URL + "/lists/" + defaultListId.get() + "/completedList").withRel("completedList"));
    }
}