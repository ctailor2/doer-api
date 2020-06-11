package com.doerapispring.web;

import com.doerapispring.authentication.AuthenticatedAuthenticationToken;
import com.doerapispring.authentication.AuthenticatedUser;
import com.doerapispring.domain.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.Clock;
import java.time.Instant;
import java.util.Date;

import static com.doerapispring.web.MockHateoasLinkGenerator.MOCK_BASE_URL;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ListsControllerTest {
    private ListsController listsController;

    private ListApplicationService listApplicationService;

    private MockMvc mockMvc;
    private AuthenticatedUser authenticatedUser;
    private TodoListModel todoListReadModel;
    private User user;
    private String listId = "someListId";
    private TodoListModelResourceTransformer todoListResourceTransformer;
    private TodoListReadModelResponse todoListReadModelResponse;

    @Before
    public void setUp() throws Exception {
        listApplicationService = mock(ListApplicationService.class);
        authenticatedUser = mock(AuthenticatedUser.class);
        user = mock(User.class);
        todoListResourceTransformer = mock(TodoListModelResourceTransformer.class);
        todoListReadModelResponse = new TodoListReadModelResponse(new TodoListReadModelDTO("someProfileName", "someName", "someDeferredName", emptyList(), emptyList(), 0L));
        when(authenticatedUser.getUser()).thenReturn(user);
        SecurityContextHolder.getContext().setAuthentication(new AuthenticatedAuthenticationToken(authenticatedUser));
        listsController = new ListsController(new MockHateoasLinkGenerator(), listApplicationService, todoListResourceTransformer, Clock.systemUTC());
        mockMvc = MockMvcBuilders
            .standaloneSetup(listsController)
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .build();

        todoListReadModel = mock(TodoListModel.class);
        when(todoListReadModel.listId()).thenReturn(new ListId(listId));
        when(listApplicationService.getDefault(any())).thenReturn(todoListReadModel);
        when(listApplicationService.get(any(), any())).thenReturn(todoListReadModel);
        when(listApplicationService.getCompleted(any(), any())).thenReturn(new CompletedTodoList(new UserId("someUserId"), new ListId(listId), emptyList()));
        when(todoListResourceTransformer.transform(any(), any())).thenReturn(todoListReadModelResponse);
    }

    @Test
    public void unlock_mapping() throws Exception {
        mockMvc.perform(post("/v1/lists/someListId/unlock"))
            .andExpect(status().isAccepted());

        verify(listApplicationService).unlock(user, new ListId("someListId"));
    }

    @Test
    public void unlock_callsTodoService_returns202() {
        String listId = "someListId";
        ResponseEntity<ResourcesResponse> responseEntity = listsController.unlock(authenticatedUser, listId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/unlockTodos").withSelfRel(),
            new Link(MOCK_BASE_URL + "/lists/" + listId).withRel("list"));
    }

    @Test
    public void show_mappings() throws Exception {
        mockMvc.perform(get("/v1/lists/someListId"))
            .andExpect(status().isOk());

        verify(listApplicationService).get(user, new ListId("someListId"));
    }

    @Test
    public void show_transformsTheListToAResource() {
        listsController.show(authenticatedUser, listId);

        verify(todoListResourceTransformer).transform(eq(todoListReadModel), any());
    }

    @Test
    public void show_returnsTheListResource() {
        ResponseEntity<TodoListReadModelResponse> actual = listsController.show(authenticatedUser, listId);

        assertThat(actual.getBody()).isEqualTo(todoListReadModelResponse);
    }

    @Test
    public void showDefault_mappings() throws Exception {
        mockMvc.perform(get("/v1/lists/default"))
            .andExpect(status().isOk());

        verify(listApplicationService).getDefault(user);
    }

    @Test
    public void showDefault_transformsTheListToAResource() {
        listsController.showDefault(authenticatedUser);

        verify(todoListResourceTransformer).transform(eq(todoListReadModel), any());
    }

    @Test
    public void showDefault_returnsTheListResource() {
        ResponseEntity<TodoListReadModelResponse> actual = listsController.showDefault(authenticatedUser);

        assertThat(actual.getBody()).isEqualTo(todoListReadModelResponse);
    }

    @Test
    public void showCompleted_mapping() throws Exception {
        mockMvc.perform(get("/v1/lists/someListId/completed"))
            .andExpect(status().isOk());

        verify(listApplicationService).getCompleted(user, new ListId("someListId"));
    }

    @Test
    public void showCompleted_returnsList() {
        String task = "someTask";
        Date completedAt = Date.from(Instant.now());
        UserId userId = new UserId("someUserId");
        ListId listId = new ListId("someListId");
        when(listApplicationService.getCompleted(any(), any())).thenReturn(
            new CompletedTodoList(
                userId,
                listId,
                singletonList(
                    new CompletedTodo(
                        new CompletedTodoId("someTodoId"),
                        task,
                        completedAt))));

        ResponseEntity<CompletedListResponse> responseEntity = listsController.showCompleted(authenticatedUser, "someListId");

        assertThat(responseEntity.getBody().getCompletedListDTO()).isNotNull();
        assertThat(responseEntity.getBody().getCompletedListDTO().getTodos()).hasSize(1);
        assertThat(responseEntity.getBody().getCompletedListDTO().getTodos().get(0).getTask()).isEqualTo(task);
        assertThat(responseEntity.getBody().getCompletedListDTO().getTodos().get(0).getCompletedAt()).isEqualTo(completedAt);
    }

    @Test
    public void showCompleted_includesLinksByDefault() {
        ResponseEntity<CompletedListResponse> responseEntity = listsController.showCompleted(authenticatedUser, "someListId");

        assertThat(responseEntity.getBody().getLinks()).contains(new Link(MOCK_BASE_URL + "/lists/someListId/completedList").withSelfRel());
    }

    @Test
    public void showAll_mapping() throws Exception {
        mockMvc.perform(get("/v1/lists"))
            .andExpect(status().isOk());

        verify(listApplicationService).getAll(user);
    }

    @Test
    public void showAll_returnsTodoLists() {
        String listName = "someName";
        when(listApplicationService.getAll(any()))
            .thenReturn(singletonList(new TodoList(new UserId("someUserId"), new ListId("someListId"), listName, 0, Date.from(Instant.EPOCH))));

        ResponseEntity<TodoListResponse> responseEntity = listsController.showAll(authenticatedUser);

        assertThat(responseEntity.getBody().getLists()).hasSize(1);
        assertThat(responseEntity.getBody().getLists().get(0).getName()).isEqualTo(listName);
    }

    @Test
    public void showAll_includesLinksByDefault() {
        when(listApplicationService.getAll(any())).thenReturn(emptyList());

        ResponseEntity<TodoListResponse> responseEntity = listsController.showAll(authenticatedUser);

        assertThat(responseEntity.getBody().getLinks())
            .contains(new Link(MOCK_BASE_URL + "/lists").withSelfRel());
    }

    @Test
    public void showAll_includesLinksForEachList() {
        String listName = "someName";
        String listId = "someListId";
        when(listApplicationService.getAll(any()))
            .thenReturn(singletonList(new TodoList(new UserId("someUserId"), new ListId(listId), listName, 0, Date.from(Instant.EPOCH))));

        ResponseEntity<TodoListResponse> responseEntity = listsController.showAll(authenticatedUser);

        assertThat(responseEntity.getBody().getLists()).hasSize(1);
        assertThat(responseEntity.getBody().getLists().get(0).getLinks())
            .contains(new Link(MOCK_BASE_URL + "/lists/" + listId).withRel("list"));
    }

    @Test
    public void create_mapping() throws Exception {
        String listName = "someName";
        mockMvc.perform(post("/v1/lists")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"name\": \"" + listName + "\"}"))
            .andExpect(status().isCreated());

        verify(listApplicationService).create(user, listName);
    }

    @Test
    public void create_includesLinksByDefault() {
        ResponseEntity<ResourcesResponse> responseEntity = listsController.create(
            authenticatedUser,
            new ListForm("someName"));

        assertThat(responseEntity.getBody().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/lists").withSelfRel(),
            new Link(MOCK_BASE_URL + "/lists").withRel("lists"));
    }

    @Test
    public void setDefault_mapping() throws Exception {
        mockMvc.perform(post("/v1/lists/someListId/default"))
            .andExpect(status().isAccepted());

        verify(listApplicationService).setDefault(user, new ListId("someListId"));
    }

    @Test
    public void setDefault_callsTodoService_returns202() {
        ResponseEntity<ResourcesResponse> responseEntity = listsController.setDefault(authenticatedUser, listId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/setDefaultList").withSelfRel(),
            new Link(MOCK_BASE_URL + "/lists/" + listId).withRel("list"));
    }
}