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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static com.doerapispring.web.MockHateoasLinkGenerator.MOCK_BASE_URL;
import static java.util.Arrays.asList;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class TodosControllerTest {
    private TodosController todosController;

    @Mock
    private TodoApiService mockTodoApiService;

    private MockMvc mockMvc;
    private AuthenticatedUser authenticatedUser;
    private List<TodoDTO> todoDTOs;

    @Before
    public void setUp() throws Exception {
        String identifier = "test@email.com";
        authenticatedUser = new AuthenticatedUser(identifier);
        SecurityContextHolder.getContext().setAuthentication(new AuthenticatedAuthenticationToken(authenticatedUser));
        todosController = new TodosController(new MockHateoasLinkGenerator(), mockTodoApiService);
        todoDTOs = asList(
            new TodoDTO("someId", "someTask", "now"),
            new TodoDTO("oneLaterId", "procrastination", "later"),
            new TodoDTO("twoLaterId", "station", "later"));
        mockMvc = MockMvcBuilders
            .standaloneSetup(todosController)
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .build();
    }

    @Test
    public void todos_mapping_callsTodoService() throws Exception {
        when(mockTodoApiService.getTodos(any())).thenReturn(new TodoListDTO(todoDTOs, false));

        mockMvc.perform(get("/v1/list/todos")).andExpect(status().isOk());

        verify(mockTodoApiService).getTodos(authenticatedUser);
    }

    @Test
    public void todos_whenInvalidRequest_returns400BadRequest() throws Exception {
        when(mockTodoApiService.getTodos(any())).thenThrow(new InvalidRequestException());

        ResponseEntity<TodosResponse> responseEntity = todosController.todos(authenticatedUser);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void todos_callsTodoService_includesLinksByDefault() throws Exception {
        when(mockTodoApiService.getTodos(any())).thenReturn(new TodoListDTO(todoDTOs, false));
        ResponseEntity<TodosResponse> responseEntity = todosController.todos(authenticatedUser);

        assertThat(responseEntity.getBody().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/list/todos").withSelfRel(),
            new Link(MOCK_BASE_URL + "/list").withRel("list"));
    }

    @Test
    public void todos_callsTodoService_byDefault_includesLinksForEachTodo() throws Exception {
        when(mockTodoApiService.getTodos(any())).thenReturn(new TodoListDTO(todoDTOs, true));
        ResponseEntity<TodosResponse> responseEntity = todosController.todos(authenticatedUser);

        assertThat(responseEntity.getBody().getTodoDTOs().get(0).getLinks()).contains(
            new Link(MOCK_BASE_URL + "/deleteTodo/someId").withRel("delete"),
            new Link(MOCK_BASE_URL + "/updateTodo/someId").withRel("update"),
            new Link(MOCK_BASE_URL + "/completeTodo/someId").withRel("complete"));
        assertThat(responseEntity.getBody().getTodoDTOs().get(0).getLinks()).containsSequence(
            new Link(MOCK_BASE_URL + "/todos/someId/moveTodo/someId").withRel("move"));
        assertThat(responseEntity.getBody().getTodoDTOs().get(1).getLinks()).containsSequence(
            new Link(MOCK_BASE_URL + "/todos/oneLaterId/moveTodo/oneLaterId").withRel("move"),
            new Link(MOCK_BASE_URL + "/todos/oneLaterId/moveTodo/twoLaterId").withRel("move"));
        assertThat(responseEntity.getBody().getTodoDTOs().get(2).getLinks()).containsSequence(
            new Link(MOCK_BASE_URL + "/todos/twoLaterId/moveTodo/oneLaterId").withRel("move"),
            new Link(MOCK_BASE_URL + "/todos/twoLaterId/moveTodo/twoLaterId").withRel("move"));
    }

    @Test
    public void todos_callsTodoService_whenListAllowsDisplacement_includesDisplaceLink_forEachNowTodo() throws Exception {
        when(mockTodoApiService.getTodos(any())).thenReturn(new TodoListDTO(todoDTOs, true));
        ResponseEntity<TodosResponse> responseEntity = todosController.todos(authenticatedUser);

        assertThat(responseEntity.getBody().getTodoDTOs().get(0).getLinks())
            .contains(new Link(MOCK_BASE_URL + "/displaceTodo/someId").withRel("displace"));
        assertThat(responseEntity.getBody().getTodoDTOs().get(1).getLinks())
            .contains(new Link(MOCK_BASE_URL + "/displaceTodo/oneLaterId").withRel("displace"));
        assertThat(responseEntity.getBody().getTodoDTOs().get(2).getLinks())
            .contains(new Link(MOCK_BASE_URL + "/displaceTodo/twoLaterId").withRel("displace"));
    }

    @Test
    public void deferredTodos_mapping_callsTodoService() throws Exception {
        when(mockTodoApiService.getDeferredTodos(any())).thenReturn(new TodoListDTO(todoDTOs, false));

        mockMvc.perform(get("/v1/list/deferredTodos")).andExpect(status().isOk());

        verify(mockTodoApiService).getDeferredTodos(authenticatedUser);
    }

    @Test
    public void deferredTodos_whenInvalidRequest_returns400BadRequest() throws Exception {
        when(mockTodoApiService.getDeferredTodos(any())).thenThrow(new InvalidRequestException());

        ResponseEntity<TodosResponse> responseEntity = todosController.deferredTodos(authenticatedUser);
        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void deferredTodos_callsTodoService_includesLinksByDefault() throws Exception {
        when(mockTodoApiService.getDeferredTodos(any())).thenReturn(new TodoListDTO(todoDTOs, false));
        ResponseEntity<TodosResponse> responseEntity = todosController.deferredTodos(authenticatedUser);

        assertThat(responseEntity.getBody().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/list/deferredTodos").withSelfRel(),
            new Link(MOCK_BASE_URL + "/list").withRel("list"));
    }

    @Test
    public void deferredTodos_callsTodoService_byDefault_includesLinksForEachTodo() throws Exception {
        when(mockTodoApiService.getDeferredTodos(any())).thenReturn(new TodoListDTO(todoDTOs, true));
        ResponseEntity<TodosResponse> responseEntity = todosController.deferredTodos(authenticatedUser);

        assertThat(responseEntity.getBody().getTodoDTOs().get(0).getLinks()).contains(
            new Link(MOCK_BASE_URL + "/deleteTodo/someId").withRel("delete"),
            new Link(MOCK_BASE_URL + "/updateTodo/someId").withRel("update"),
            new Link(MOCK_BASE_URL + "/completeTodo/someId").withRel("complete"));
        assertThat(responseEntity.getBody().getTodoDTOs().get(0).getLinks()).containsSequence(
            new Link(MOCK_BASE_URL + "/todos/someId/moveTodo/someId").withRel("move"));
        assertThat(responseEntity.getBody().getTodoDTOs().get(1).getLinks()).containsSequence(
            new Link(MOCK_BASE_URL + "/todos/oneLaterId/moveTodo/oneLaterId").withRel("move"),
            new Link(MOCK_BASE_URL + "/todos/oneLaterId/moveTodo/twoLaterId").withRel("move"));
        assertThat(responseEntity.getBody().getTodoDTOs().get(2).getLinks()).containsSequence(
            new Link(MOCK_BASE_URL + "/todos/twoLaterId/moveTodo/oneLaterId").withRel("move"),
            new Link(MOCK_BASE_URL + "/todos/twoLaterId/moveTodo/twoLaterId").withRel("move"));
    }

    @Test
    public void deferredTodos_callsTodoService_whenListAllowsDisplacement_includesDisplaceLink_forEachNowTodo() throws Exception {
        when(mockTodoApiService.getDeferredTodos(any())).thenReturn(new TodoListDTO(todoDTOs, true));
        ResponseEntity<TodosResponse> responseEntity = todosController.deferredTodos(authenticatedUser);

        assertThat(responseEntity.getBody().getTodoDTOs().get(0).getLinks())
            .contains(new Link(MOCK_BASE_URL + "/displaceTodo/someId").withRel("displace"));
        assertThat(responseEntity.getBody().getTodoDTOs().get(1).getLinks())
            .contains(new Link(MOCK_BASE_URL + "/displaceTodo/oneLaterId").withRel("displace"));
        assertThat(responseEntity.getBody().getTodoDTOs().get(2).getLinks())
            .contains(new Link(MOCK_BASE_URL + "/displaceTodo/twoLaterId").withRel("displace"));
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

        verify(mockTodoApiService).delete(authenticatedUser, localId);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getLinks()).containsOnly(
            new Link(MOCK_BASE_URL + "/deleteTodo/someId").withSelfRel(),
            new Link(MOCK_BASE_URL + "/list").withRel("list"));
    }

    @Test
    public void delete_whenInvalidRequest_returns400BadRequest() throws Exception {
        doThrow(new InvalidRequestException()).when(mockTodoApiService).delete(any(), any());

        ResponseEntity<ResourcesResponse> responseEntity = todosController.delete(authenticatedUser, "someId");

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void displace_mapping() throws Exception {
        mockMvc.perform(post("/v1/todos/123/displace")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"task\": \"return redbox movie\"}"))
            .andExpect(status().isAccepted());
    }

    @Test
    public void displace_callsTodoService_returns202() throws Exception {
        String localId = "someId";
        String task = "some task";
        ResponseEntity<ResourcesResponse> responseEntity =
            todosController.displace(authenticatedUser, localId, new TodoForm(task));

        verify(mockTodoApiService).displace(authenticatedUser, localId, task);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getLinks()).containsOnly(
            new Link(MOCK_BASE_URL + "/displaceTodo/someId").withSelfRel(),
            new Link(MOCK_BASE_URL + "/list").withRel("list"));
    }

    @Test
    public void displace_whenInvalidRequest_returns400BadRequest() throws Exception {
        doThrow(new InvalidRequestException()).when(mockTodoApiService).displace(any(), any(), any());

        ResponseEntity<ResourcesResponse> responseEntity = todosController.displace(authenticatedUser, "someId", new TodoForm("do it to it"));

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
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

        verify(mockTodoApiService).update(authenticatedUser, localId, task);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getLinks()).contains(
            new Link(MOCK_BASE_URL + "/updateTodo/someId").withSelfRel(),
            new Link(MOCK_BASE_URL + "/list").withRel("list"));
    }

    @Test
    public void update_whenInvalidRequest_returns400BadRequest() throws Exception {
        doThrow(new InvalidRequestException()).when(mockTodoApiService).update(any(), any(), any());

        ResponseEntity<ResourcesResponse> responseEntity = todosController.update(authenticatedUser, "someId", new TodoForm("do it to it"));

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
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

        verify(mockTodoApiService).complete(authenticatedUser, localId);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getLinks()).containsOnly(
            new Link(MOCK_BASE_URL + "/completeTodo/someId").withSelfRel(),
            new Link(MOCK_BASE_URL + "/list").withRel("list"));
    }

    @Test
    public void complete_whenInvalidRequest_returns400BadRequest() throws Exception {
        doThrow(new InvalidRequestException()).when(mockTodoApiService).complete(any(), any());

        ResponseEntity<ResourcesResponse> responseEntity = todosController.complete(authenticatedUser, "someId");

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void completedTodos_mapping() throws Exception {
        when(mockTodoApiService.getCompleted(any())).thenReturn(new CompletedTodoListDTO(todoDTOs));

        mockMvc.perform(get("/v1/completedTodos"))
            .andExpect(status().isOk());
    }

    @Test
    public void completedTodos_callsTodoService_includesLinksByDefault() throws Exception {
        when(mockTodoApiService.getCompleted(any())).thenReturn(new CompletedTodoListDTO(todoDTOs));

        ResponseEntity<TodosResponse> responseEntity = todosController.completedTodos(authenticatedUser);

        verify(mockTodoApiService).getCompleted(authenticatedUser);
        assertThat(responseEntity.getBody().getLinks()).containsOnly(
            new Link(MOCK_BASE_URL + "/completedTodos").withSelfRel(),
            new Link(MOCK_BASE_URL + "/list").withRel("list"));
    }

    @Test
    public void completedTodos_whenInvalidRequest_returns400BadRequest() throws Exception {
        when(mockTodoApiService.getCompleted(any())).thenThrow(new InvalidRequestException());

        ResponseEntity<TodosResponse> responseEntity = todosController.completedTodos(authenticatedUser);
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

        verify(mockTodoApiService).move(authenticatedUser, localId, targetLocalId);
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getLinks()).containsOnly(
            new Link(MOCK_BASE_URL + "/todos/localId/moveTodo/targetLocalId").withSelfRel(),
            new Link(MOCK_BASE_URL + "/list").withRel("list"));
    }

    @Test
    public void move_whenInvalidRequest_returns400BadRequest() throws Exception {
        doThrow(new InvalidRequestException()).when(mockTodoApiService).move(any(), any(), any());

        ResponseEntity<ResourcesResponse> responseEntity = todosController.move(authenticatedUser, "localId", "targetLocalId");

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void pull_mapping() throws Exception {
        mockMvc.perform(post("/v1/list/pull"))
            .andExpect(status().isAccepted());

        verify(mockTodoApiService).pull(authenticatedUser);
    }

    @Test
    public void pull_callsTodoService_returns202() throws Exception {
        ResponseEntity<ResourcesResponse> responseEntity = todosController.pull(authenticatedUser);

        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.ACCEPTED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getLinks()).containsOnly(
            new Link(MOCK_BASE_URL + "/list/pullTodos").withSelfRel(),
            new Link(MOCK_BASE_URL + "/list").withRel("list"));
    }

    @Test
    public void pull_whenInvalidRequest_returns400BadRequest() throws Exception {
        doThrow(new InvalidRequestException()).when(mockTodoApiService).pull(any());

        ResponseEntity<ResourcesResponse> responseEntity = todosController.pull(authenticatedUser);

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void create_mapping() throws Exception {
        mockMvc.perform(post("/v1/list/todos")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"task\": \"return redbox movie\"}"))
            .andExpect(status().isCreated());
    }

    @Test
    public void create_callsTokenService_returns201() throws Exception {
        String task = "some task";
        TodoForm todoForm = new TodoForm(task);
        ResponseEntity<ResourcesResponse> responseEntity = todosController.create(authenticatedUser, todoForm);

        verify(mockTodoApiService).create(authenticatedUser, task, "now");
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getLinks()).containsOnly(
            new Link(MOCK_BASE_URL + "/list/createTodo").withSelfRel(),
            new Link(MOCK_BASE_URL + "/list").withRel("list"));
    }

    @Test
    public void create_whenInvalidRequest_returns400BadRequest() throws Exception {
        doThrow(new InvalidRequestException()).when(mockTodoApiService).create(any(), any(), any());

        ResponseEntity responseEntity = todosController.create(authenticatedUser, new TodoForm("someTask"));

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void createDeferred_mapping() throws Exception {
        mockMvc.perform(post("/v1/list/deferredTodos")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"task\": \"return redbox movie\"}"))
            .andExpect(status().isCreated());
    }

    @Test
    public void createDeferred_callsTokenService_returns201() throws Exception {
        String task = "some task";
        TodoForm todoForm = new TodoForm(task);
        ResponseEntity<ResourcesResponse> responseEntity = todosController.createDeferred(authenticatedUser, todoForm);

        verify(mockTodoApiService).create(authenticatedUser, task, "later");
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getLinks()).containsOnly(
            new Link(MOCK_BASE_URL + "/list/createDeferredTodo").withSelfRel(),
            new Link(MOCK_BASE_URL + "/list").withRel("list"));
    }

    @Test
    public void createDeferred_whenInvalidRequest_returns400BadRequest() throws Exception {
        doThrow(new InvalidRequestException()).when(mockTodoApiService).create(any(), any(), any());

        ResponseEntity responseEntity = todosController.createDeferred(authenticatedUser, new TodoForm("someTask"));

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}