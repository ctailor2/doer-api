package com.doerapispring.userSessions;

import com.doerapispring.Credentials;
import com.doerapispring.LoginForm;
import com.doerapispring.SignupForm;
import com.doerapispring.UserIdentifier;
import com.doerapispring.apiTokens.SessionTokenService;
import com.doerapispring.users.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by chiragtailor on 8/12/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class UserSessionsControllerTest {
    private UserSessionsController userSessionsController;

    @Mock
    private UserService userService;

    @Mock
    private SessionTokenService sessionTokenService;

    @Mock
    private AuthenticationService authenticationService;

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
    public void logout_mapping() throws Exception {
        mockMvc.perform(post("/v1/logout")
                .header("Session-Token", "tokenz"))
                .andExpect(status().isOk());
    }

    @Test
    public void logout_callsUserSessionsService() throws Exception {
        userSessionsController.logout("test@email.com");
        verify(userSessionsService).logout("test@email.com");
    }
}