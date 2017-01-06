package com.doerapispring.web;

import com.doerapispring.authentication.AuthenticatedAuthenticationToken;
import com.doerapispring.authentication.AuthenticatedUser;
import com.doerapispring.domain.*;
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
    private TodoService todoService;

    private MockMvc mockMvc;
    private User user;

    @Before
    public void setUp() throws Exception {
        String identifier = "test@email.com";
        UniqueIdentifier uniqueIdentifier = new UniqueIdentifier(identifier);
        user = new User(uniqueIdentifier);
        SecurityContextHolder.getContext().setAuthentication(new AuthenticatedAuthenticationToken(new AuthenticatedUser(identifier)));
        todosController = new TodosController(todoService);
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
        verify(todoService).getByScheduling(user, ScheduledFor.anytime);
    }

    @Test
    public void index_withValidSchedulingSpecified_callsTodoService_withSpecifiedType() throws Exception {
        todosController.index(AuthenticatedUser.identifiedWith("test@email.com"), ScheduledFor.now.toString());
        verify(todoService).getByScheduling(user, ScheduledFor.now);
    }

    @Test
    public void index_withInvalidSchedulingSpecified_returns400() throws Exception {
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
        ScheduledFor scheduling = ScheduledFor.later;
        TodoForm todoForm = new TodoForm(task, scheduling);
        todosController.create(AuthenticatedUser.identifiedWith("test@email.com"), todoForm);
        verify(todoService).create(user, task, scheduling);
    }

    @Test
    public void create_whenOperationRefused_returns400BadRequest() throws Exception {
        when(todoService.create(any(), any(), any())).thenThrow(new OperationRefusedException());

        ResponseEntity<Todo> responseEntity = todosController.create(AuthenticatedUser.identifiedWith("test@email.com"), new TodoForm("something", ScheduledFor.now));

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}