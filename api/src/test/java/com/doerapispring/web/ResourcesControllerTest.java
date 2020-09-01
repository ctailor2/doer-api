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
import scala.jdk.javaapi.CollectionConverters;

import java.util.Collections;

import static com.doerapispring.web.MockHateoasLinkGenerator.MOCK_BASE_URL;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ResourcesControllerTest {
    private MockMvc mockMvc;
    private ResourcesController resourcesController;
    private AuthenticatedUser authenticatedUser;
    private ListId defaultListId;

    @Before
    public void setUp() throws Exception {
        String identifier = "test@email.com";
        authenticatedUser = mock(AuthenticatedUser.class);
        User user = new User(new UserId(identifier), new ListId("someListId"));
        when(authenticatedUser.getUser()).thenReturn(user);
        SecurityContextHolder.getContext().setAuthentication(new AuthenticatedAuthenticationToken(authenticatedUser));
        ListApplicationService listApplicationService = mock(ListApplicationService.class);
        resourcesController = new ResourcesController(new MockHateoasLinkGenerator(), listApplicationService);
        mockMvc = MockMvcBuilders
            .standaloneSetup(resourcesController)
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .build();
        defaultListId = new ListId("someListId");
        java.util.List<DeprecatedTodo> emptyList = Collections.emptyList();
        when(listApplicationService.getDefault(any())).thenReturn(
            new DeprecatedTodoListModel(defaultListId, "someName", CollectionConverters.asScala(emptyList).toList(), null, null, null, null));
    }

    @Test
    public void base_mapping() throws Exception {
        mockMvc.perform(get("/v1/"))
            .andExpect(status().isOk());
    }

    @Test
    public void base_includesLinks() {
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
    public void root_includesLinks() {
        ResponseEntity<ResourcesResponse> responseEntity = resourcesController.root();

        assertThat(responseEntity.getBody().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/rootResources").withSelfRel(),
            new Link(MOCK_BASE_URL + "/listResources").withRel("listResources"),
            new Link(MOCK_BASE_URL + "/historyResources").withRel("historyResources"));
    }

    @Test
    public void list_mapping() throws Exception {
        mockMvc.perform(get("/v1/resources/list"))
            .andExpect(status().isOk());
    }

    @Test
    public void list_includesLinksByDefault() {
        ResponseEntity<ResourcesResponse> responseEntity = resourcesController.list(authenticatedUser);

        assertThat(responseEntity.getBody().getLinks()).containsOnly(
            new Link(MOCK_BASE_URL + "/listResources").withSelfRel(),
            new Link(MOCK_BASE_URL + "/lists/defaultList").withRel("list"),
            new Link(MOCK_BASE_URL + "/lists").withRel("lists"),
            new Link(MOCK_BASE_URL + "/lists").withRel("createList"));
    }

    @Test
    public void history_mapping() throws Exception {
        mockMvc.perform(get("/v1/resources/history"))
            .andExpect(status().isOk());
    }

    @Test
    public void history_includesLinks() {
        ResponseEntity<ResourcesResponse> responseEntity = resourcesController.history(authenticatedUser);

        assertThat(responseEntity.getBody().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/historyResources").withSelfRel(),
            new Link(MOCK_BASE_URL + "/lists/" + defaultListId.get() + "/completedList").withRel("completedList"));
    }
}