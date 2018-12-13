package com.doerapispring.web;

import com.doerapispring.authentication.AuthenticatedAuthenticationToken;
import com.doerapispring.authentication.AuthenticatedUser;
import com.doerapispring.domain.TodoApplicationService;
import com.doerapispring.domain.TodoId;
import com.doerapispring.domain.User;
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

import static com.doerapispring.web.MockHateoasLinkGenerator.MOCK_BASE_URL;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class TodosControllerTest {
    private TodosController todosController;

    private TodoApplicationService todoApplicationService;

    private MockMvc mockMvc;
    private AuthenticatedUser authenticatedUser;
    private User user;

    @Before
    public void setUp() throws Exception {
        todoApplicationService = mock(TodoApplicationService.class);
        authenticatedUser = mock(AuthenticatedUser.class);
        user = mock(User.class);
        when(authenticatedUser.getUser()).thenReturn(user);
        SecurityContextHolder.getContext().setAuthentication(new AuthenticatedAuthenticationToken(authenticatedUser));
        todosController = new TodosController(new MockHateoasLinkGenerator(), todoApplicationService);
        mockMvc = MockMvcBuilders
            .standaloneSetup(todosController)
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .build();
    }

    @Test
    public void delete_mapping() throws Exception {
        mockMvc.perform(delete("/v1/todos/someId"))
            .andExpect(status().isAccepted());
    }

    @Test
    public void delete_callsTodoService_returns202() throws Exception {
        String localId = "someId";
        ResponseEntity<ResourcesResponse> responseEntity = todosController.delete(authenticatedUser, localId);

        verify(todoApplicationService).delete(user, new TodoId(localId));
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getLinks()).containsOnly(
            new Link(MOCK_BASE_URL + "/deleteTodo/someId").withSelfRel(),
            new Link(MOCK_BASE_URL + "/list").withRel("list"));
    }

    @Test
    public void delete_whenInvalidRequest_returns400BadRequest() throws Exception {
        doThrow(new InvalidRequestException()).when(todoApplicationService).delete(any(), any());

        ResponseEntity<ResourcesResponse> responseEntity = todosController.delete(authenticatedUser, "someId");

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void displace_mapping() throws Exception {
        mockMvc.perform(post("/v1/list/displace")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"task\": \"return redbox movie\"}"))
            .andExpect(status().isAccepted());
    }

    @Test
    public void displace_callsTodoService_returns202() throws Exception {
        String task = "some task";
        ResponseEntity<ResourcesResponse> responseEntity =
            todosController.displace(authenticatedUser, new TodoForm(task));

        verify(todoApplicationService).displace(user, task);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getLinks()).containsOnly(
            new Link(MOCK_BASE_URL + "/list/displaceTodo").withSelfRel(),
            new Link(MOCK_BASE_URL + "/list").withRel("list"));
    }

    @Test
    public void update_mapping() throws Exception {
        mockMvc.perform(put("/v1/todos/123")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"task\": \"return redbox movie\"}"))
            .andExpect(status().isAccepted());
    }

    @Test
    public void update_callsTodoService_returns202() throws Exception {
        String localId = "someId";
        String task = "some task";
        ResponseEntity<ResourcesResponse> responseEntity =
            todosController.update(authenticatedUser, localId, new TodoForm(task));

        verify(todoApplicationService).update(user, new TodoId(localId), task);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/updateTodo/someId").withSelfRel(),
            new Link(MOCK_BASE_URL + "/list").withRel("list"));
    }

    @Test
    public void complete_mapping() throws Exception {
        mockMvc.perform(post("/v1/todos/123/complete"))
            .andExpect(status().isAccepted());
    }

    @Test
    public void complete_callsTodoService_returns202() throws Exception {
        String localId = "someId";
        ResponseEntity<ResourcesResponse> responseEntity =
            todosController.complete(authenticatedUser, localId);

        verify(todoApplicationService).complete(user, new TodoId(localId));
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getLinks()).containsOnly(
            new Link(MOCK_BASE_URL + "/completeTodo/someId").withSelfRel(),
            new Link(MOCK_BASE_URL + "/list").withRel("list"));
    }

    @Test
    public void complete_whenInvalidRequest_returns400BadRequest() throws Exception {
        doThrow(new InvalidRequestException()).when(todoApplicationService).complete(any(), any());

        ResponseEntity<ResourcesResponse> responseEntity = todosController.complete(authenticatedUser, "someId");

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void move_mapping() throws Exception {
        mockMvc.perform(post("/v1/todos/1/move/3"))
            .andExpect(status().isAccepted());
    }

    @Test
    public void move_callsTodoService_returns202() throws Exception {
        String localId = "localId";
        String targetLocalId = "targetLocalId";
        ResponseEntity<ResourcesResponse> responseEntity =
            todosController.move(authenticatedUser, localId, targetLocalId);

        verify(todoApplicationService).move(user, new TodoId(localId), new TodoId(targetLocalId));
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getLinks()).containsOnly(
            new Link(MOCK_BASE_URL + "/todos/localId/moveTodo/targetLocalId").withSelfRel(),
            new Link(MOCK_BASE_URL + "/list").withRel("list"));
    }

    @Test
    public void move_whenInvalidRequest_returns400BadRequest() throws Exception {
        doThrow(new InvalidRequestException()).when(todoApplicationService).move(any(), any(), any());

        ResponseEntity<ResourcesResponse> responseEntity = todosController.move(authenticatedUser, "localId", "targetLocalId");

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void pull_mapping() throws Exception {
        mockMvc.perform(post("/v1/list/pull"))
            .andExpect(status().isAccepted());

        verify(todoApplicationService).pull(user);
    }

    @Test
    public void pull_responseIncludesLinks() throws Exception {
        ResponseEntity<ResourcesResponse> responseEntity = todosController.pull(authenticatedUser);

        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getLinks()).containsOnly(
            new Link(MOCK_BASE_URL + "/list/pullTodos").withSelfRel(),
            new Link(MOCK_BASE_URL + "/list").withRel("list"));
    }

    @Test
    public void pull_whenInvalidRequest_returns400BadRequest() throws Exception {
        doThrow(new InvalidRequestException()).when(todoApplicationService).pull(any());

        ResponseEntity<ResourcesResponse> responseEntity = todosController.pull(authenticatedUser);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void escalate_mapping() throws Exception {
        mockMvc.perform(post("/v1/list/escalate"))
            .andExpect(status().isAccepted());

        verify(todoApplicationService).escalate(user);
    }

    @Test
    public void escalate_responseIncludesLinks() throws Exception {
        ResponseEntity<ResourcesResponse> responseEntity = todosController.escalate(authenticatedUser);

        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getLinks()).containsOnly(
            new Link(MOCK_BASE_URL + "/list/escalateTodo").withSelfRel(),
            new Link(MOCK_BASE_URL + "/list").withRel("list"));
    }

    @Test
    public void create_mapping() throws Exception {
        mockMvc.perform(post("/v1/list/todos")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"task\": \"return redbox movie\"}"))
            .andExpect(status().isCreated());
    }

    @Test
    public void create_callsTodoService_returns201() throws Exception {
        String task = "some task";
        TodoForm todoForm = new TodoForm(task);
        ResponseEntity<ResourcesResponse> responseEntity = todosController.create(authenticatedUser, todoForm);

        verify(todoApplicationService).create(user, task);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getLinks()).containsOnly(
            new Link(MOCK_BASE_URL + "/list/createTodo").withSelfRel(),
            new Link(MOCK_BASE_URL + "/list").withRel("list"));
    }

    @Test
    public void createDeferred_mapping() throws Exception {
        mockMvc.perform(post("/v1/list/deferredTodos")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"task\": \"return redbox movie\"}"))
            .andExpect(status().isCreated());
    }

    @Test
    public void createDeferred_callsTodoService_returns201() throws Exception {
        String task = "some task";
        TodoForm todoForm = new TodoForm(task);
        ResponseEntity<ResourcesResponse> responseEntity = todosController.createDeferred(authenticatedUser, todoForm);

        verify(todoApplicationService).createDeferred(user, task);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getLinks()).containsOnly(
            new Link(MOCK_BASE_URL + "/list/createDeferredTodo").withSelfRel(),
            new Link(MOCK_BASE_URL + "/list").withRel("list"));
    }
}