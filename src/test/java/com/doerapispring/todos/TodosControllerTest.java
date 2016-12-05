package com.doerapispring.todos;

import com.doerapispring.UserIdentifier;
import com.doerapispring.apiTokens.AuthenticatedAuthenticationToken;
import com.doerapispring.apiTokens.AuthenticatedUser;
import com.doerapispring.userSessions.OperationRefusedException;
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

/**
 * Created by chiragtailor on 9/27/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class TodosControllerTest {
    private TodosController todosController;

    @Mock
    private TodoService todoService;

    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        SecurityContextHolder.getContext().setAuthentication(new AuthenticatedAuthenticationToken(new AuthenticatedUser(new UserIdentifier("test"))));
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
    public void index_withNoTypeSpecified_callsTodoService_withNullType() throws Exception {
        todosController.index(AuthenticatedUser.identifiedWith(new UserIdentifier("test@email.com")), null);
        verify(todoService).get("test@email.com", null);
    }

    @Test
    public void index_withValidTypeSpecified_callsTodoService_withSpecifiedType() throws Exception {
        todosController.index(AuthenticatedUser.identifiedWith(new UserIdentifier("test@email.com")), TodoTypeParamEnum.active);
        verify(todoService).get("test@email.com", TodoTypeParamEnum.active);
    }

    @Test
    public void index_withInvalidTypeSpecified_returns400() throws Exception {
        mockMvc.perform(get("/v1/todos")
                .param("type", "notARealType"))
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
        UserIdentifier userIdentifier = new UserIdentifier("test@email.com");
        String task = "browse the web";
        ScheduledFor scheduling = ScheduledFor.later;
        TodoForm todoForm = new TodoForm(task, scheduling);
        todosController.create(AuthenticatedUser.identifiedWith(userIdentifier), todoForm);
        verify(todoService).newCreate(userIdentifier, task, scheduling);
    }

    @Test
    public void create_whenOperationRefused_returns400BadRequest() throws Exception {
        when(todoService.newCreate(any(), any(), any())).thenThrow(new OperationRefusedException());

        UserIdentifier userIdentifier = new UserIdentifier("test@email.com");
        ResponseEntity<NewTodo> responseEntity = todosController.create(AuthenticatedUser.identifiedWith(userIdentifier), new TodoForm("something", ScheduledFor.now));

        assertThat(responseEntity).isNotNull();
        assertThat(responseEntity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }
}