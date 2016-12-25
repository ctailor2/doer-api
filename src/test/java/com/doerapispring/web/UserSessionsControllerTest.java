package com.doerapispring.web;

import com.doerapispring.authentication.AccessDeniedException;
import com.doerapispring.authentication.Credentials;
import com.doerapispring.authentication.UserSessionsService;
import com.doerapispring.domain.OperationRefusedException;
import com.doerapispring.domain.UserIdentifier;
import com.doerapispring.domain.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(MockitoJUnitRunner.class)
public class UserSessionsControllerTest {
    private UserSessionsController userSessionsController;

    @Mock
    private UserService userService;

    @Mock
    private UserSessionsService userSessionsService;

    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        userSessionsController = new UserSessionsController(userSessionsService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(userSessionsController)
                .build();
    }

    @Test
    public void signup_mapping() throws Exception {
        mockMvc.perform(post("/v1/signup")
                .accept(MediaType.APPLICATION_JSON)
                .content("{}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated());
    }

    @Test
    public void signup_callsUserSessionsService() throws Exception {
        UserIdentifier userIdentifier = new UserIdentifier("soUnique");
        Credentials credentials = new Credentials("soSecure");
        SignupForm signupForm = new SignupForm(userIdentifier, credentials);
        userSessionsController.signup(signupForm);
        verify(userSessionsService).signup(userIdentifier, credentials);
    }

    @Test
    public void signup_whenOperationRefused_returns400BadRequest() throws Exception {
        when(userSessionsService.signup(any(), any())).thenThrow(OperationRefusedException.class);
        mockMvc.perform(post("/v1/signup")
                .accept(MediaType.APPLICATION_JSON)
                .content("{}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isBadRequest());

    }

    @Test
    public void login_mapping() throws Exception {
        mockMvc.perform(post("/v1/login")
                .accept(MediaType.APPLICATION_JSON)
                .content("{\"user\":{\"email\":\"test@email.com\"}}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    @Test
    public void login_callsUserSessionsService() throws Exception {
        UserIdentifier userIdentifier = new UserIdentifier("soUnique");
        Credentials credentials = new Credentials("soSecure");
        LoginForm loginForm = new LoginForm(userIdentifier, credentials);
        userSessionsController.login(loginForm);
        verify(userSessionsService).login(userIdentifier, credentials);
    }

    @Test
    public void login_whenAccessDenied_returns401Unauthorized() throws Exception {
        when(userSessionsService.login(any(), any())).thenThrow(AccessDeniedException.class);
        mockMvc.perform(post("/v1/login")
                .accept(MediaType.APPLICATION_JSON)
                .content("{\"user\":{\"email\":\"test@email.com\"}}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}