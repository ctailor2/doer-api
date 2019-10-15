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

import java.time.Instant;
import java.util.Date;
import java.util.List;

import static com.doerapispring.web.MockHateoasLinkGenerator.MOCK_BASE_URL;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ListsControllerTest {
    private ListsController listsController;

    private ListApplicationService listApplicationService;

    private MockMvc mockMvc;
    private AuthenticatedUser authenticatedUser;
    private TodoListReadModel todoListReadModel;
    private User user;
    private String listId = "someListId";

    @Before
    public void setUp() throws Exception {
        listApplicationService = mock(ListApplicationService.class);
        authenticatedUser = mock(AuthenticatedUser.class);
        user = mock(User.class);
        when(authenticatedUser.getUser()).thenReturn(user);
        SecurityContextHolder.getContext().setAuthentication(new AuthenticatedAuthenticationToken(authenticatedUser));
        listsController = new ListsController(new MockHateoasLinkGenerator(), listApplicationService);
        mockMvc = MockMvcBuilders
            .standaloneSetup(listsController)
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .build();

        todoListReadModel = mock(TodoListReadModel.class);
        when(todoListReadModel.getListId()).thenReturn(new ListId(listId));
        when(listApplicationService.getDefault(any())).thenReturn(mock(TodoList.class));
        when(listApplicationService.get(any(), any())).thenReturn(todoListReadModel);
    }

    @Test
    public void unlock_mapping() throws Exception {
        mockMvc.perform(post("/v1/lists/someListId/unlock"))
            .andExpect(status().isAccepted());

        verify(listApplicationService).unlock(user, new ListId("someListId"));
    }

    @Test
    public void unlock_callsTodoService_returns202() throws Exception {
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
    public void show_returnsList() throws Exception {
        String profileName = "someProfileName";
        when(todoListReadModel.getProfileName()).thenReturn(profileName);
        String name = "someName";
        when(todoListReadModel.getSectionName()).thenReturn(name);
        String deferredName = "someDeferredName";
        when(todoListReadModel.getDeferredSectionName()).thenReturn(deferredName);
        Todo todo = new Todo(new TodoId("oneNowId"), "oneNowTask");
        when(todoListReadModel.getTodos()).thenReturn(singletonList(todo));
        Todo deferredTodo = new Todo(new TodoId("oneLaterId"), "oneLaterTask");
        when(todoListReadModel.getDeferredTodos()).thenReturn(singletonList(deferredTodo));
        long unlockDuration = 123213L;
        when(todoListReadModel.unlockDuration()).thenReturn(unlockDuration);

        ResponseEntity<TodoListReadModelResponse> responseEntity = listsController.show(authenticatedUser, listId);

        TodoListReadModelDTO todoListReadModelDTO = responseEntity.getBody().getTodoListReadModelDTO();
        assertThat(todoListReadModelDTO).isNotNull();
        assertThat(todoListReadModelDTO.getProfileName()).isEqualTo(profileName);
        assertThat(todoListReadModelDTO.getName()).isEqualTo(name);
        assertThat(todoListReadModelDTO.getDeferredName()).isEqualTo(deferredName);
        assertThat(todoListReadModelDTO.getTodos()).hasSize(1);
        assertThat(todoListReadModelDTO.getTodos().get(0).getIdentifier()).isEqualTo(todo.getTodoId().getIdentifier());
        assertThat(todoListReadModelDTO.getTodos().get(0).getTask()).isEqualTo(todo.getTask());
        assertThat(todoListReadModelDTO.getDeferredTodos()).hasSize(1);
        assertThat(todoListReadModelDTO.getDeferredTodos().get(0).getIdentifier()).isEqualTo(deferredTodo.getTodoId().getIdentifier());
        assertThat(todoListReadModelDTO.getDeferredTodos().get(0).getTask()).isEqualTo(deferredTodo.getTask());
        assertThat(todoListReadModelDTO.getUnlockDuration()).isEqualTo(unlockDuration);
    }

    @Test
    public void show_includesLinks_byDefault() throws Exception {
        ResponseEntity<TodoListReadModelResponse> responseEntity = listsController.show(authenticatedUser, listId);

        assertThat(responseEntity.getBody().getLinks()).contains(new Link(MOCK_BASE_URL + "/lists/" + listId).withSelfRel());
        assertThat(responseEntity.getBody().getTodoListReadModelDTO().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/createDeferredTodo").withRel("createDeferred"));
    }

    @Test
    public void show_returnsList_includesLinksForEachTodo() throws Exception {
        List<Todo> todos = asList(
            new Todo(new TodoId("oneNowId"), "oneNowTask"),
            new Todo(new TodoId("twoNowId"), "twoNowTask"));
        when(todoListReadModel.getTodos()).thenReturn(todos);
        List<Todo> deferredTodos = asList(
            new Todo(new TodoId("oneLaterId"), "oneLaterTask"),
            new Todo(new TodoId("twoLaterId"), "twoLaterTask"));
        when(todoListReadModel.getDeferredTodos()).thenReturn(deferredTodos);

        ResponseEntity<TodoListReadModelResponse> responseEntity = listsController.show(authenticatedUser, listId);

        assertThat(responseEntity.getBody().getTodoListReadModelDTO().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/createDeferredTodo").withRel("createDeferred"));
        assertThat(responseEntity.getBody().getTodoListReadModelDTO().getTodos()).hasSize(2);
        assertThat(responseEntity.getBody().getTodoListReadModelDTO().getTodos().get(0).getLinks()).contains(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/deleteTodo/oneNowId").withRel("delete"),
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/updateTodo/oneNowId").withRel("update"),
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/completeTodo/oneNowId").withRel("complete"));
        assertThat(responseEntity.getBody().getTodoListReadModelDTO().getTodos().get(0).getLinks()).contains(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/deleteTodo/oneNowId").withRel("delete"),
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/updateTodo/oneNowId").withRel("update"),
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/completeTodo/oneNowId").withRel("complete"));
        assertThat(responseEntity.getBody().getTodoListReadModelDTO().getTodos().get(0).getLinks()).containsSequence(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/todos/oneNowId/moveTodo/oneNowId").withRel("move"),
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/todos/oneNowId/moveTodo/twoNowId").withRel("move"));
        assertThat(responseEntity.getBody().getTodoListReadModelDTO().getTodos().get(1).getLinks()).containsSequence(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/todos/twoNowId/moveTodo/oneNowId").withRel("move"),
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/todos/twoNowId/moveTodo/twoNowId").withRel("move"));
        assertThat(responseEntity.getBody().getTodoListReadModelDTO().getDeferredTodos()).hasSize(2);
        assertThat(responseEntity.getBody().getTodoListReadModelDTO().getDeferredTodos().get(0).getLinks()).contains(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/deleteTodo/oneLaterId").withRel("delete"),
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/updateTodo/oneLaterId").withRel("update"),
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/completeTodo/oneLaterId").withRel("complete"));
        assertThat(responseEntity.getBody().getTodoListReadModelDTO().getDeferredTodos().get(0).getLinks()).contains(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/deleteTodo/oneLaterId").withRel("delete"),
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/updateTodo/oneLaterId").withRel("update"),
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/completeTodo/oneLaterId").withRel("complete"));
        assertThat(responseEntity.getBody().getTodoListReadModelDTO().getDeferredTodos().get(0).getLinks()).containsSequence(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/todos/oneLaterId/moveTodo/oneLaterId").withRel("move"),
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/todos/oneLaterId/moveTodo/twoLaterId").withRel("move"));
        assertThat(responseEntity.getBody().getTodoListReadModelDTO().getDeferredTodos().get(1).getLinks()).containsSequence(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/todos/twoLaterId/moveTodo/oneLaterId").withRel("move"),
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/todos/twoLaterId/moveTodo/twoLaterId").withRel("move"));
    }

    @Test
    public void show_whenListIsNotFull_includesCreateLink_doesNotIncludeDisplaceLink() throws Exception {
        when(todoListReadModel.isFull()).thenReturn(false);

        ResponseEntity<TodoListReadModelResponse> responseEntity = listsController.show(authenticatedUser, listId);

        assertThat(responseEntity.getBody().getTodoListReadModelDTO().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/createTodo").withRel("create"));
        assertThat(responseEntity.getBody().getTodoListReadModelDTO().getLinks()).doesNotContain(
            new Link(MOCK_BASE_URL + "/list/displaceTodo").withRel("displace"));
    }

    @Test
    public void show_whenListIsFull_doesNotIncludeCreateLink_includesDisplaceLink() throws Exception {
        when(todoListReadModel.isFull()).thenReturn(true);

        ResponseEntity<TodoListReadModelResponse> responseEntity = listsController.show(authenticatedUser, listId);

        assertThat(responseEntity.getBody().getTodoListReadModelDTO().getLinks()).doesNotContain(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/createTodo").withRel("create"));
        assertThat(responseEntity.getBody().getTodoListReadModelDTO().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/displaceTodo").withRel("displace"));
    }

    @Test
    public void show_whenListIsAbleToBeReplenished_includesPullLink() throws Exception {
        when(todoListReadModel.isAbleToBeReplenished()).thenReturn(true);

        ResponseEntity<TodoListReadModelResponse> responseEntity = listsController.show(authenticatedUser, listId);

        assertThat(responseEntity.getBody().getTodoListReadModelDTO().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/pullTodos").withRel("pull"));
    }

    @Test
    public void show_whenListIsNotAbleToBeReplenished_doesNotIncludePullLink() throws Exception {
        when(todoListReadModel.isAbleToBeReplenished()).thenReturn(false);

        ResponseEntity<TodoListReadModelResponse> responseEntity = listsController.show(authenticatedUser, listId);

        assertThat(responseEntity.getBody().getTodoListReadModelDTO().getLinks()).doesNotContain(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/pullTodos").withRel("pull"));
    }

    @Test
    public void show_whenListIsAbleToBeUnlocked_includesUnlockLink() throws Exception {
        when(todoListReadModel.isAbleToBeUnlocked()).thenReturn(true);

        ResponseEntity<TodoListReadModelResponse> responseEntity = listsController.show(authenticatedUser, listId);

        assertThat(responseEntity.getBody().getTodoListReadModelDTO().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/unlockTodos").withRel("unlock"));
    }

    @Test
    public void show_whenListIsNotAbleToBeUnlocked_doesNotIncludeUnlockLink() throws Exception {
        when(todoListReadModel.isAbleToBeUnlocked()).thenReturn(false);

        ResponseEntity<TodoListReadModelResponse> responseEntity = listsController.show(authenticatedUser, listId);

        assertThat(responseEntity.getBody().getTodoListReadModelDTO().getLinks()).doesNotContain(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/unlockTodos").withRel("unlock"));
    }

    @Test
    public void show_whenListIsAbleToBeEscalated_includesEscalateLink() throws Exception {
        when(todoListReadModel.isAbleToBeEscalated()).thenReturn(true);

        ResponseEntity<TodoListReadModelResponse> responseEntity = listsController.show(authenticatedUser, listId);

        assertThat(responseEntity.getBody().getTodoListReadModelDTO().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/escalateTodo").withRel("escalate"));
    }

    @Test
    public void show_whenListIsNotAbleToBeEscalated_doesNotIncludeEscalateLink() throws Exception {
        when(todoListReadModel.isAbleToBeEscalated()).thenReturn(false);

        ResponseEntity<TodoListReadModelResponse> responseEntity = listsController.show(authenticatedUser, listId);

        assertThat(responseEntity.getBody().getTodoListReadModelDTO().getLinks()).doesNotContain(
            new Link(MOCK_BASE_URL + "/list/escalateTodo").withRel("escalate"));
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
        when(listApplicationService.getCompleted(any(), any())).thenReturn(singletonList(new CompletedTodo(
            new UserId("someUserId"),
            new ListId("someListId"),
            new CompletedTodoId("someTodoId"),
            task,
            completedAt)));

        ResponseEntity<CompletedListResponse> responseEntity = listsController.showCompleted(authenticatedUser, "someListId");

        assertThat(responseEntity.getBody().getCompletedListDTO()).isNotNull();
        assertThat(responseEntity.getBody().getCompletedListDTO().getTodos()).hasSize(1);
        assertThat(responseEntity.getBody().getCompletedListDTO().getTodos().get(0).getTask()).isEqualTo(task);
        assertThat(responseEntity.getBody().getCompletedListDTO().getTodos().get(0).getCompletedAt()).isEqualTo(completedAt);
    }

    @Test
    public void showCompleted_includesLinksByDefault() throws Exception {
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
            new Link(MOCK_BASE_URL + "/lists").withSelfRel());
    }
}