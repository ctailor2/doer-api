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

import java.util.Collections;
import java.util.List;

import static com.doerapispring.web.MockHateoasLinkGenerator.MOCK_BASE_URL;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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
        todoDTOs = Collections.singletonList(new TodoDTO("someId", "someTask", "now"));
        when(mockTodoApiService.get(any())).thenReturn(new TodoList(todoDTOs, false));
        mockMvc = MockMvcBuilders
                .standaloneSetup(todosController)
                .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
                .build();
    }

    @Test
    public void index_mapping() throws Exception {
        mockMvc.perform(get("/v1/todos"))
                .andExpect(status().isOk());
    }

    @Test
    public void index_callsTodoService_includesLinksByDefault() throws Exception {
        ResponseEntity<TodosResponse> responseEntity = todosController.index(authenticatedUser);

        verify(mockTodoApiService).get(authenticatedUser);
        assertThat(responseEntity.getBody().getLinks())
                .contains(new Link(MOCK_BASE_URL + "/todos").withSelfRel(),
                        new Link(MOCK_BASE_URL + "/createTodoForLater").withRel("todoLater"));
    }

    @Test
    public void index_callsTodoService_whenListAllowsSchedulingTasksForNow_includesLink() throws Exception {
        when(mockTodoApiService.get(any())).thenReturn(new TodoList(todoDTOs, true));
        ResponseEntity<TodosResponse> responseEntity = todosController.index(authenticatedUser);

        verify(mockTodoApiService).get(authenticatedUser);
        assertThat(responseEntity.getBody().getLinks())
                .contains(new Link(MOCK_BASE_URL + "/createTodoForNow").withRel("todoNow"));
    }

    @Test
    public void createForNow_mapping() throws Exception {
        mockMvc.perform(post("/v1/todoNow")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"task\": \"return redbox movie\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    public void createForNow_callsTokenService_returns201() throws Exception {
        String task = "some task";
        TodoForm todoForm = new TodoForm(task);
        ResponseEntity<CreateTodoResponse> responseEntity = todosController.createForNow(authenticatedUser, todoForm);

        verify(mockTodoApiService).create(authenticatedUser, task, "now");
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getLinks()).contains(
                new Link(MOCK_BASE_URL + "/createTodoForNow").withSelfRel(),
                new Link(MOCK_BASE_URL + "/todos").withRel("todos"));
    }

    @Test
    public void createForNow_whenInvalidRequest_returns400BadRequest() throws Exception {
        doThrow(new InvalidRequestException()).when(mockTodoApiService).create(any(), any(), any());

        ResponseEntity responseEntity = todosController.createForNow(authenticatedUser, new TodoForm("some task"));

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    public void createForLater_mapping() throws Exception {
        mockMvc.perform(post("/v1/todoLater")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"task\": \"return redbox movie\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    public void createForLater_callsTokenService_returns201() throws Exception {
        String task = "some task";
        TodoForm todoForm = new TodoForm(task);
        ResponseEntity<CreateTodoResponse> responseEntity = todosController.createForLater(authenticatedUser, todoForm);

        verify(mockTodoApiService).create(authenticatedUser, task, "later");
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(responseEntity.getBody()).isNotNull();
        assertThat(responseEntity.getBody().getLinks()).contains(
                new Link(MOCK_BASE_URL + "/createTodoForLater").withSelfRel(),
                new Link(MOCK_BASE_URL + "/todos").withRel("todos"));   }

    @Test
    public void createForLater_whenInvalidRequest_returns400BadRequest() throws Exception {
        doThrow(new InvalidRequestException()).when(mockTodoApiService).create(any(), any(), any());

        ResponseEntity responseEntity = todosController.createForLater(authenticatedUser, new TodoForm("some task"));

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}