package com.doerapispring.web;

import com.doerapispring.authentication.AuthenticatedAuthenticationToken;
import com.doerapispring.authentication.AuthenticatedUser;
import com.doerapispring.domain.ScheduledFor;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class TodosControllerTest {
    private TodosController todosController;

    @Mock
    private TodoApiService todoApiService;

    private MockMvc mockMvc;
    private AuthenticatedUser authenticatedUser;

    @Before
    public void setUp() throws Exception {
        String identifier = "test@email.com";
        authenticatedUser = new AuthenticatedUser(identifier);
        SecurityContextHolder.getContext().setAuthentication(new AuthenticatedAuthenticationToken(authenticatedUser));
        todosController = new TodosController(todoApiService);
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
    public void index_withNoSchedulingSpecified_callsTodoService_withAnytime() throws Exception {
        mockMvc.perform(get("/v1/todos"));
        verify(todoApiService).getByScheduling(authenticatedUser, "anytime");
    }

    @Test
    public void index_withValidSchedulingSpecified_callsTodoService_withSpecifiedType() throws Exception {
        todosController.index(AuthenticatedUser.identifiedWith("test@email.com"), ScheduledFor.now.toString());
        verify(todoApiService).getByScheduling(authenticatedUser, "now");
    }

    @Test
    public void index_whenRequestInvalid_returns400() throws Exception {
        when(todoApiService.getByScheduling(any(), any())).thenThrow(new InvalidRequestException());

        mockMvc.perform(get("/v1/todos")
                .param("scheduling", "notARealScheduling"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void create_mapping() throws Exception {
        mockMvc.perform(post("/v1/todos")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"task\": \"return redbox movie\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    public void create_callsTokenService() throws Exception {
        String task = "browse the web";
        String scheduling = "later";
        TodoForm todoForm = new TodoForm(task, scheduling);
        todosController.create(AuthenticatedUser.identifiedWith("test@email.com"), todoForm);
        verify(todoApiService).create(authenticatedUser, task, scheduling);
    }

    @Test
    public void create_whenInvalidRequest_returns400BadRequest() throws Exception {
        when(todoApiService.create(any(), any(), any())).thenThrow(new InvalidRequestException());

        ResponseEntity<TodoDTO> responseEntity = todosController.create(AuthenticatedUser.identifiedWith("test@email.com"), new TodoForm("something", "now"));

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}