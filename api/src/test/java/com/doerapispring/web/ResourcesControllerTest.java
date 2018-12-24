package com.doerapispring.web;

import com.doerapispring.authentication.AuthenticatedAuthenticationToken;
import com.doerapispring.authentication.AuthenticatedUser;
import com.doerapispring.domain.ListApplicationService;
import com.doerapispring.domain.ListId;
import com.doerapispring.domain.ReadOnlyTodoList;
import com.doerapispring.domain.User;
import org.junit.Before;
import org.junit.Test;
import org.springframework.hateoas.Link;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.Collections;

import static com.doerapispring.web.MockHateoasLinkGenerator.MOCK_BASE_URL;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
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

    @Before
    public void setUp() throws Exception {
        String identifier = "test@email.com";
        authenticatedUser = mock(AuthenticatedUser.class);
        user = mock(User.class);
        when(authenticatedUser.getUser()).thenReturn(user);
        SecurityContextHolder.getContext().setAuthentication(new AuthenticatedAuthenticationToken(authenticatedUser));
        listApplicationService = mock(ListApplicationService.class);
        resourcesController = new ResourcesController(new MockHateoasLinkGenerator(), listApplicationService);
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
        when(listApplicationService.getAll(any())).thenReturn(singletonList(
            new ReadOnlyTodoList(null, null, null, emptyList(), null, new ListId("someListId"))));
        mockMvc.perform(get("/v1/resources/todo"))
                .andExpect(status().isOk());

        verify(listApplicationService).getAll(user);
    }

    @Test
    public void todo_includesLinksByDefault() throws Exception {
        ListId firstListId = new ListId("firstListId");
        when(listApplicationService.getAll(any())).thenReturn(asList(
            new ReadOnlyTodoList(null, null, null, emptyList(), null, firstListId),
            new ReadOnlyTodoList(null, null, null, emptyList(), null, new ListId("secondListId"))));

        ResponseEntity<ResourcesResponse> responseEntity = resourcesController.todo(authenticatedUser);

        assertThat(responseEntity.getBody().getLinks()).containsOnly(
                new Link(MOCK_BASE_URL + "/todoResources").withSelfRel(),
                new Link(MOCK_BASE_URL + "/lists/firstListId").withRel("list"));
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
                new Link(MOCK_BASE_URL + "/completedList").withRel("completedList"));
    }
}