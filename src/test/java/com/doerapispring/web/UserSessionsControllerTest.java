package com.doerapispring.web;

import com.doerapispring.authentication.AccessDeniedException;
import com.doerapispring.domain.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.hateoas.Link;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static com.doerapispring.web.MockHateoasLinkGenerator.MOCK_BASE_URL;
import static org.assertj.core.api.Assertions.assertThat;
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
    private UserSessionsApiService mockUserSessionsApiService;

    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        userSessionsController = new UserSessionsController(new MockHateoasLinkGenerator(), mockUserSessionsApiService);
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
    public void signup_callsUserSessionsApiService_includesLinks() throws Exception {
        String identifier = "soUnique";
        String credentials = "soSecure";
        SignupForm signupForm = new SignupForm(identifier, credentials);
        ResponseEntity<SessionResponse> responseEntity = userSessionsController.signup(signupForm);
        verify(mockUserSessionsApiService).signup(identifier, credentials);
        assertThat(responseEntity.getBody().getLinks()).contains(
                new Link(MOCK_BASE_URL + "/signup").withSelfRel(),
                new Link(MOCK_BASE_URL + "/rootResources").withRel("root"));
    }

    @Test
    public void signup_whenAccessDenied_returns400BadRequest() throws Exception {
        when(mockUserSessionsApiService.signup(any(), any())).thenThrow(AccessDeniedException.class);
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
    public void login_callsUserSessionsService_includesLinks() throws Exception {
        String credentials = "soSecure";
        String identifier = "soUnique";
        LoginForm loginForm = new LoginForm(identifier, credentials);
        ResponseEntity<SessionResponse> responseEntity = userSessionsController.login(loginForm);
        verify(mockUserSessionsApiService).login(identifier, credentials);
        assertThat(responseEntity.getBody().getLinks()).contains(
                new Link(MOCK_BASE_URL + "/login").withSelfRel(),
                new Link(MOCK_BASE_URL + "/rootResources").withRel("root"));
    }

    @Test
    public void login_whenAccessDenied_returns401Unauthorized() throws Exception {
        when(mockUserSessionsApiService.login(any(), any())).thenThrow(AccessDeniedException.class);
        mockMvc.perform(post("/v1/login")
                .accept(MediaType.APPLICATION_JSON)
                .content("{\"user\":{\"email\":\"test@email.com\"}}")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized());
    }
}