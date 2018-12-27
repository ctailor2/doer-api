package com.doerapispring.web;

import com.doerapispring.authentication.AuthenticatedAuthenticationToken;
import com.doerapispring.authentication.AuthenticatedUser;
import com.doerapispring.domain.*;
import org.junit.Before;
import org.junit.Test;
import org.springframework.hateoas.Link;
import org.springframework.http.HttpStatus;
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
import static java.util.Collections.singletonList;
import static org.fest.assertions.api.Assertions.assertThat;
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
    private ReadOnlyTodoList readOnlyTodoList;
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

        readOnlyTodoList = mock(ReadOnlyTodoList.class);
        when(readOnlyTodoList.getListId()).thenReturn(new ListId(listId));
        when(listApplicationService.get(any())).thenReturn(readOnlyTodoList);
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
    public void unlock_whenInvalidRequest_returns400BadRequest() throws Exception {
        doThrow(new InvalidRequestException()).when(listApplicationService).unlock(any(), any());

        ResponseEntity<ResourcesResponse> responseEntity = listsController.unlock(authenticatedUser, "someListId");

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void show_mappings() throws Exception {
        mockMvc.perform(get("/v1/list"))
            .andExpect(status().isOk());

        mockMvc.perform(get("/v1/lists/someListId"))
            .andExpect(status().isOk());
    }

    @Test
    public void show_returnsList() throws Exception {
        String name = "someName";
        when(readOnlyTodoList.getSectionName()).thenReturn(name);
        String deferredName = "someDeferredName";
        when(readOnlyTodoList.getDeferredSectionName()).thenReturn(deferredName);
        Todo todo = new Todo(new TodoId("oneNowId"), "oneNowTask");
        when(readOnlyTodoList.getTodos()).thenReturn(singletonList(todo));
        Todo deferredTodo = new Todo(new TodoId("oneLaterId"), "oneLaterTask");
        when(readOnlyTodoList.getDeferredTodos()).thenReturn(singletonList(deferredTodo));
        long unlockDuration = 123213L;
        when(readOnlyTodoList.unlockDuration()).thenReturn(unlockDuration);

        ResponseEntity<TodoListResponse> responseEntity = listsController.show(authenticatedUser, listId);

        TodoListDTO todoListDTO = responseEntity.getBody().getTodoListDTO();
        assertThat(todoListDTO).isNotNull();
        assertThat(todoListDTO.getName()).isEqualTo(name);
        assertThat(todoListDTO.getDeferredName()).isEqualTo(deferredName);
        assertThat(todoListDTO.getTodos()).hasSize(1);
        assertThat(todoListDTO.getTodos().get(0).getIdentifier()).isEqualTo(todo.getTodoId().getIdentifier());
        assertThat(todoListDTO.getTodos().get(0).getTask()).isEqualTo(todo.getTask());
        assertThat(todoListDTO.getDeferredTodos()).hasSize(1);
        assertThat(todoListDTO.getDeferredTodos().get(0).getIdentifier()).isEqualTo(deferredTodo.getTodoId().getIdentifier());
        assertThat(todoListDTO.getDeferredTodos().get(0).getTask()).isEqualTo(deferredTodo.getTask());
        assertThat(todoListDTO.getUnlockDuration()).isEqualTo(unlockDuration);
    }

    @Test
    public void show_includesLinks_byDefault() throws Exception {
        ResponseEntity<TodoListResponse> responseEntity = listsController.show(authenticatedUser, listId);

        assertThat(responseEntity.getBody().getLinks()).contains(new Link(MOCK_BASE_URL + "/lists/" + listId).withSelfRel());
        assertThat(responseEntity.getBody().getTodoListDTO().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/createDeferredTodo").withRel("createDeferred"));
    }

    @Test
    public void show_returnsList_includesLinksForEachTodo() throws Exception {
        List<Todo> todos = asList(
            new Todo(new TodoId("oneNowId"), "oneNowTask"),
            new Todo(new TodoId("twoNowId"), "twoNowTask"));
        when(readOnlyTodoList.getTodos()).thenReturn(todos);
        List<Todo> deferredTodos = asList(
            new Todo(new TodoId("oneLaterId"), "oneLaterTask"),
            new Todo(new TodoId("twoLaterId"), "twoLaterTask"));
        when(readOnlyTodoList.getDeferredTodos()).thenReturn(deferredTodos);

        ResponseEntity<TodoListResponse> responseEntity = listsController.show(authenticatedUser, listId);

        assertThat(responseEntity.getBody().getTodoListDTO().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/createDeferredTodo").withRel("createDeferred"));
        assertThat(responseEntity.getBody().getTodoListDTO().getTodos()).hasSize(2);
        assertThat(responseEntity.getBody().getTodoListDTO().getTodos().get(0).getLinks()).contains(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/deleteTodo/oneNowId").withRel("delete"),
            new Link(MOCK_BASE_URL + "/updateTodo/oneNowId").withRel("update"),
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/completeTodo/oneNowId").withRel("complete"));
        assertThat(responseEntity.getBody().getTodoListDTO().getTodos().get(0).getLinks()).contains(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/deleteTodo/oneNowId").withRel("delete"),
            new Link(MOCK_BASE_URL + "/updateTodo/oneNowId").withRel("update"),
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/completeTodo/oneNowId").withRel("complete"));
        assertThat(responseEntity.getBody().getTodoListDTO().getTodos().get(0).getLinks()).containsSequence(
            new Link(MOCK_BASE_URL + "/todos/oneNowId/moveTodo/oneNowId").withRel("move"),
            new Link(MOCK_BASE_URL + "/todos/oneNowId/moveTodo/twoNowId").withRel("move"));
        assertThat(responseEntity.getBody().getTodoListDTO().getTodos().get(1).getLinks()).containsSequence(
            new Link(MOCK_BASE_URL + "/todos/twoNowId/moveTodo/oneNowId").withRel("move"),
            new Link(MOCK_BASE_URL + "/todos/twoNowId/moveTodo/twoNowId").withRel("move"));
        assertThat(responseEntity.getBody().getTodoListDTO().getDeferredTodos()).hasSize(2);
        assertThat(responseEntity.getBody().getTodoListDTO().getDeferredTodos().get(0).getLinks()).contains(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/deleteTodo/oneLaterId").withRel("delete"),
            new Link(MOCK_BASE_URL + "/updateTodo/oneLaterId").withRel("update"),
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/completeTodo/oneLaterId").withRel("complete"));
        assertThat(responseEntity.getBody().getTodoListDTO().getDeferredTodos().get(0).getLinks()).contains(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/deleteTodo/oneLaterId").withRel("delete"),
            new Link(MOCK_BASE_URL + "/updateTodo/oneLaterId").withRel("update"),
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/completeTodo/oneLaterId").withRel("complete"));
        assertThat(responseEntity.getBody().getTodoListDTO().getDeferredTodos().get(0).getLinks()).containsSequence(
            new Link(MOCK_BASE_URL + "/todos/oneLaterId/moveTodo/oneLaterId").withRel("move"),
            new Link(MOCK_BASE_URL + "/todos/oneLaterId/moveTodo/twoLaterId").withRel("move"));
        assertThat(responseEntity.getBody().getTodoListDTO().getDeferredTodos().get(1).getLinks()).containsSequence(
            new Link(MOCK_BASE_URL + "/todos/twoLaterId/moveTodo/oneLaterId").withRel("move"),
            new Link(MOCK_BASE_URL + "/todos/twoLaterId/moveTodo/twoLaterId").withRel("move"));
    }

    @Test
    public void show_whenListIsNotFull_includesCreateLink_doesNotIncludeDisplaceLink() throws Exception {
        when(readOnlyTodoList.isFull()).thenReturn(false);

        ResponseEntity<TodoListResponse> responseEntity = listsController.show(authenticatedUser, listId);

        assertThat(responseEntity.getBody().getTodoListDTO().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/createTodo").withRel("create"));
        assertThat(responseEntity.getBody().getTodoListDTO().getLinks()).doesNotContain(
            new Link(MOCK_BASE_URL + "/list/displaceTodo").withRel("displace"));
    }

    @Test
    public void show_whenListIsFull_doesNotIncludeCreateLink_includesDisplaceLink() throws Exception {
        when(readOnlyTodoList.isFull()).thenReturn(true);

        ResponseEntity<TodoListResponse> responseEntity = listsController.show(authenticatedUser, listId);

        assertThat(responseEntity.getBody().getTodoListDTO().getLinks()).doesNotContain(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/createTodo").withRel("create"));
        assertThat(responseEntity.getBody().getTodoListDTO().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/displaceTodo").withRel("displace"));
    }

    @Test
    public void show_whenListIsAbleToBeReplenished_includesPullLink() throws Exception {
        when(readOnlyTodoList.isAbleToBeReplenished()).thenReturn(true);

        ResponseEntity<TodoListResponse> responseEntity = listsController.show(authenticatedUser, listId);

        assertThat(responseEntity.getBody().getTodoListDTO().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/pullTodos").withRel("pull"));
    }

    @Test
    public void show_whenListIsNotAbleToBeReplenished_doesNotIncludePullLink() throws Exception {
        when(readOnlyTodoList.isAbleToBeReplenished()).thenReturn(false);

        ResponseEntity<TodoListResponse> responseEntity = listsController.show(authenticatedUser, listId);

        assertThat(responseEntity.getBody().getTodoListDTO().getLinks()).doesNotContain(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/pullTodos").withRel("pull"));
    }

    @Test
    public void show_whenListIsAbleToBeUnlocked_includesUnlockLink() throws Exception {
        when(readOnlyTodoList.isAbleToBeUnlocked()).thenReturn(true);

        ResponseEntity<TodoListResponse> responseEntity = listsController.show(authenticatedUser, listId);

        assertThat(responseEntity.getBody().getTodoListDTO().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/unlockTodos").withRel("unlock"));
    }

    @Test
    public void show_whenListIsNotAbleToBeUnlocked_doesNotIncludeUnlockLink() throws Exception {
        when(readOnlyTodoList.isAbleToBeUnlocked()).thenReturn(false);

        ResponseEntity<TodoListResponse> responseEntity = listsController.show(authenticatedUser, listId);

        assertThat(responseEntity.getBody().getTodoListDTO().getLinks()).doesNotContain(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/unlockTodos").withRel("unlock"));
    }

    @Test
    public void show_whenListIsAbleToBeEscalated_includesEscalateLink() throws Exception {
        when(readOnlyTodoList.isAbleToBeEscalated()).thenReturn(true);

        ResponseEntity<TodoListResponse> responseEntity = listsController.show(authenticatedUser, listId);

        assertThat(responseEntity.getBody().getTodoListDTO().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/escalateTodo").withRel("escalate"));
    }

    @Test
    public void show_whenListIsNotAbleToBeEscalated_doesNotIncludeEscalateLink() throws Exception {
        when(readOnlyTodoList.isAbleToBeEscalated()).thenReturn(false);

        ResponseEntity<TodoListResponse> responseEntity = listsController.show(authenticatedUser, listId);

        assertThat(responseEntity.getBody().getTodoListDTO().getLinks()).doesNotContain(
            new Link(MOCK_BASE_URL + "/list/escalateTodo").withRel("escalate"));
    }

    @Test
    public void show_whenInvalidRequest_throws400BadRequest() throws Exception {
        when(listApplicationService.get(any())).thenThrow(new InvalidRequestException());

        ResponseEntity<TodoListResponse> responseEntity = listsController.show(authenticatedUser, listId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void showCompleted_mapping() throws Exception {
        mockMvc.perform(get("/v1/completedList"))
            .andExpect(status().isOk());

        verify(listApplicationService).getCompleted(user);
    }

    @Test
    public void showCompleted_returnsList() throws InvalidRequestException {
        String task = "someTask";
        Date completedAt = Date.from(Instant.now());
        when(listApplicationService.getCompleted(any())).thenReturn(singletonList(new CompletedTodo(
            new UserId("someUserId"),
            new CompletedTodoId("someTodoId"),
            task,
            completedAt)));

        ResponseEntity<CompletedListResponse> responseEntity = listsController.showCompleted(authenticatedUser);

        assertThat(responseEntity.getBody().getCompletedListDTO()).isNotNull();
        assertThat(responseEntity.getBody().getCompletedListDTO().getTodos()).hasSize(1);
        assertThat(responseEntity.getBody().getCompletedListDTO().getTodos().get(0).getTask()).isEqualTo(task);
        assertThat(responseEntity.getBody().getCompletedListDTO().getTodos().get(0).getCompletedAt()).isEqualTo(completedAt);
        assertThat(responseEntity.getBody().getLinks()).contains(new Link(MOCK_BASE_URL + "/completedList").withSelfRel());
    }

    @Test
    public void showCompleted_includesLinksByDefault() throws Exception {
        ResponseEntity<CompletedListResponse> responseEntity = listsController.showCompleted(authenticatedUser);

        assertThat(responseEntity.getBody().getLinks()).contains(new Link(MOCK_BASE_URL + "/completedList").withSelfRel());
    }

    @Test
    public void showCompleted_whenInvalidRequest_throws400BadRequest() throws Exception {
        when(listApplicationService.getCompleted(any())).thenThrow(new InvalidRequestException());

        ResponseEntity<CompletedListResponse> responseEntity = listsController.showCompleted(authenticatedUser);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}