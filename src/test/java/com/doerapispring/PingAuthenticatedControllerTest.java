package com.doerapispring;

import com.doerapispring.userSessions.AuthenticationService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Created by chiragtailor on 9/5/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class PingAuthenticatedControllerTest {
    private PingAuthenticatedController pingAuthenticatedController;

    @Mock
    private AuthenticationService authenticationService;

    private MockMvc mockMvc;

    @Before
    public void setUp() throws Exception {
        pingAuthenticatedController = new PingAuthenticatedController(authenticationService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(pingAuthenticatedController)
                .build();
    }

    @Test
    public void pingAuthenticated_mapping() throws Exception {
        mockMvc.perform(get("/v1/pingAuthenticated"))
                .andExpect(status().isBadRequest());
    }

    @Test
    public void pingAuthenticated_callsAuthenticationService_withSessionTokenHeader() throws Exception {
        pingAuthenticatedController.pingAuthenticated("token");

        verify(authenticationService).authenticateSessionToken("token");
    }
}