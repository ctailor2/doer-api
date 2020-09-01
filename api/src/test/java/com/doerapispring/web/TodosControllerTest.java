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
import scala.jdk.javaapi.CollectionConverters;
import scala.util.Success;

import java.time.Clock;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.doerapispring.web.MockHateoasLinkGenerator.MOCK_BASE_URL;
import static java.util.Collections.emptyList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TodosControllerTest {
    private TodosController todosController;

    private MockMvc mockMvc;
    private AuthenticatedUser authenticatedUser;

    @Before
    public void setUp() throws Exception {
        TodoApplicationService todoApplicationService = mock(TodoApplicationService.class);
        authenticatedUser = mock(AuthenticatedUser.class);
        User user = mock(User.class);
        when(authenticatedUser.getUser()).thenReturn(user);
        SecurityContextHolder.getContext().setAuthentication(new AuthenticatedAuthenticationToken(authenticatedUser));
        TodoListModelResourceTransformer todoListModelResourceTransformer = mock(TodoListModelResourceTransformer.class);
        todosController = new TodosController(new MockHateoasLinkGenerator(), todoApplicationService, todoListModelResourceTransformer, Clock.systemUTC());
        mockMvc = MockMvcBuilders
            .standaloneSetup(todosController)
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .build();
        when(todoListModelResourceTransformer.transform(any(), any())).thenReturn(new TodoListReadModelResponse(null));
        java.util.List<DeprecatedTodo> todos = emptyList();
        DeprecatedTodoListModel todoListModel = new DeprecatedTodoListModel(new ListId("someListId"), "someName", CollectionConverters.asScala(todos).toList(), null, null, null, null);
        when(todoApplicationService.performOperation(any(), any(), any(Supplier.class), any()))
                .thenReturn(new Success<>(todoListModel));
        when(todoApplicationService.performOperation(any(), any(), any(Function.class), any()))
                .thenReturn(new Success<>(todoListModel));
    }

    @Test
    public void delete_mapping() throws Exception {
        mockMvc.perform(delete("/v1/lists/someListId/todos/someTodoId"))
            .andExpect(status().isAccepted());
    }

    @Test
    public void delete_returns202() {
        ResponseEntity<TodoListReadModelResponse> responseEntity = todosController.delete(authenticatedUser, "someListId", "someTodoId");

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getLinks()).containsOnly(
            new Link(MOCK_BASE_URL + "/lists/someListId/deleteTodo/someTodoId").withSelfRel(),
            new Link(MOCK_BASE_URL + "/lists/someListId").withRel("list"));
    }

    @Test
    public void displace_mapping() throws Exception {
        mockMvc.perform(post("/v1/lists/someListId/displace")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"task\": \"return redbox movie\"}"))
            .andExpect(status().isAccepted());
    }

    @Test
    public void displace_callsTodoService_returns202() {
        String listId = "someListId";
        ResponseEntity<TodoListReadModelResponse> responseEntity =
            todosController.displace(authenticatedUser, listId, new TodoForm("some task"));

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getLinks()).containsOnly(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/displaceTodo").withSelfRel(),
            new Link(MOCK_BASE_URL + "/lists/" + listId).withRel("list"));
    }

    @Test
    public void update_mapping() throws Exception {
        mockMvc.perform(put("/v1/lists/someListId/todos/123")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"task\": \"return redbox movie\"}"))
            .andExpect(status().isAccepted());
    }

    @Test
    public void update_callsTodoService_returns202() {
        String listId = "someListId";
        ResponseEntity<TodoListReadModelResponse> responseEntity =
            todosController.update(authenticatedUser, listId, "someId", new TodoForm("some task"));

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/updateTodo/someId").withSelfRel(),
            new Link(MOCK_BASE_URL + "/lists/" + listId).withRel("list"));
    }

    @Test
    public void complete_mapping() throws Exception {
        mockMvc.perform(post("/v1/lists/someListId/todos/someTodoId/complete"))
            .andExpect(status().isAccepted());
    }

    @Test
    public void complete_returns202() {
        ResponseEntity<TodoListReadModelResponse> responseEntity =
            todosController.complete(authenticatedUser, "someListId", "someTodoId");

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getLinks()).containsOnly(
            new Link(MOCK_BASE_URL + "/lists/someListId/completeTodo/someTodoId").withSelfRel(),
            new Link(MOCK_BASE_URL + "/lists/someListId").withRel("list"));
    }

    @Test
    public void move_mapping() throws Exception {
        mockMvc.perform(post("/v1/lists/someListId/todos/1/move/3"))
            .andExpect(status().isAccepted());
    }

    @Test
    public void move_returns202() {
        String listId = "someListId";
        String targetTodoId = "targetTodoId";
        ResponseEntity<TodoListReadModelResponse> responseEntity =
            todosController.move(authenticatedUser, listId, "todoId", targetTodoId);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getLinks()).containsOnly(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/todos/todoId/moveTodo/targetTodoId").withSelfRel(),
            new Link(MOCK_BASE_URL + "/lists/" + listId).withRel("list"));
    }

    @Test
    public void pull_mapping() throws Exception {
        mockMvc.perform(post("/v1/lists/someListId/pull"))
            .andExpect(status().isAccepted());
    }

    @Test
    public void pull_responseIncludesLinks() {
        String listId = "someListId";
        ResponseEntity<TodoListReadModelResponse> responseEntity = todosController.pull(authenticatedUser, listId);

        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getLinks()).containsOnly(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/pullTodos").withSelfRel(),
            new Link(MOCK_BASE_URL + "/lists/" + listId).withRel("list"));
    }

    @Test
    public void escalate_mapping() throws Exception {
        mockMvc.perform(post("/v1/lists/someListId/escalate"))
            .andExpect(status().isAccepted());
    }

    @Test
    public void escalate_responseIncludesLinks() {
        String listId = "someListId";
        ResponseEntity<TodoListReadModelResponse> responseEntity = todosController.escalate(authenticatedUser, listId);

        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getLinks()).containsOnly(
            new Link(MOCK_BASE_URL + "/lists/" + listId + "/escalateTodo").withSelfRel(),
            new Link(MOCK_BASE_URL + "/lists/" + listId).withRel("list"));
    }

    @Test
    public void create_mapping() throws Exception {
        mockMvc.perform(post("/v1/lists/someListId/todos")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"task\": \"return redbox movie\"}"))
            .andExpect(status().isCreated());
    }

    @Test
    public void create_returns201() {
        String listIdPathVariable = "someListId";
        ResponseEntity<TodoListReadModelResponse> responseEntity = todosController.create(authenticatedUser, listIdPathVariable, new TodoForm("some task"));

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getLinks()).containsOnly(
            new Link(MOCK_BASE_URL + "/lists/" + listIdPathVariable + "/createTodo").withSelfRel(),
            new Link(MOCK_BASE_URL + "/lists/" + listIdPathVariable).withRel("list"));
    }

    @Test
    public void createDeferred_mapping() throws Exception {
        mockMvc.perform(post("/v1/lists/someListId/deferredTodos")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"task\": \"return redbox movie\"}"))
            .andExpect(status().isCreated());
    }

    @Test
    public void createDeferred_returns201() {
        String listIdPathVariable = "someListId";
        ResponseEntity<TodoListReadModelResponse> responseEntity = todosController.createDeferred(authenticatedUser, listIdPathVariable, new TodoForm("some task"));

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getLinks()).containsOnly(
            new Link(MOCK_BASE_URL + "/lists/" + listIdPathVariable + "/createDeferredTodo").withSelfRel(),
            new Link(MOCK_BASE_URL + "/lists/" + listIdPathVariable).withRel("list"));
    }
}