package com.doerapispring.userSessions;

import com.doerapispring.apiTokens.SessionToken;
import com.doerapispring.apiTokens.SessionTokenService;
import com.doerapispring.users.User;
import com.doerapispring.users.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Created by chiragtailor on 9/1/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class UserSessionsServiceTest {
    private UserSessionsService userSessionsService;

    @Mock
    private UserService userService;

    @Mock
    private SessionTokenService sessionTokenService;

    @Mock
    private AuthenticationService authenticationService;

    private User user;
    private SessionToken sessionToken;

    @Before
    public void setUp() throws Exception {
        userSessionsService = new UserSessionsService(userService, sessionTokenService, authenticationService);

        user = User.builder()
                .email("test@email.com")
                .password("password")
                .build();

        sessionToken = SessionToken.builder()
                .token("superSecureToken")
                .build();
    }

    @Test
    public void signup_callsUserService_callsSessionTokenService_returnsUser() throws Exception {
        doReturn(user).when(userService).create(user);
        doReturn(sessionToken).when(sessionTokenService).create("test@email.com");

        User resultUser = userSessionsService.signup(user);

        verify(userService).create(user);
        verify(sessionTokenService).create("test@email.com");

        assertThat(resultUser.getEmail()).isEqualTo("test@email.com");
        assertThat(resultUser.getSessionToken().getToken()).isEqualTo("superSecureToken");
    }

    @Test
    public void login_callsAuthenticationService_whenAuthenticationSuccessful_getsTokenFromSessionTokenService_returnsUser() throws Exception {
        doReturn(true).when(authenticationService).authenticate("test@email.com", "password");
        doReturn(sessionToken).when(sessionTokenService).getActive("test@email.com");

        User resultUser = userSessionsService.login(user);

        verify(sessionTokenService).getActive("test@email.com");

        assertThat(resultUser.getEmail()).isEqualTo("test@email.com");
        assertThat(resultUser.getSessionToken().getToken()).isEqualTo("superSecureToken");
    }

    @Test
    public void login_callsAuthenticationService_whenAuthenticationSuccessful_whenTokenDoesNotExist_createsTokenWithSessionTokenService_returnsUser() throws Exception {
        doReturn(true).when(authenticationService).authenticate("test@email.com", "password");
        doReturn(null).when(sessionTokenService).getActive("test@email.com");
        doReturn(sessionToken).when(sessionTokenService).create("test@email.com");

        User resultUser = userSessionsService.login(user);

        verify(authenticationService).authenticate("test@email.com", "password");
        verify(sessionTokenService).getActive("test@email.com");
        verify(sessionTokenService).create("test@email.com");

        assertThat(resultUser.getEmail()).isEqualTo("test@email.com");
        assertThat(resultUser.getSessionToken().getToken()).isEqualTo("superSecureToken");
    }

    @Test
    public void login_callsAuthenticationService_whenAuthenticationFails_doesNotCallSessionTokenService_returnsNull() throws Exception {
        doReturn(false).when(authenticationService).authenticate("test@email.com", "password");

        User resultUser = userSessionsService.login(user);

        verify(authenticationService).authenticate("test@email.com", "password");
        verifyZeroInteractions(sessionTokenService);

        assertThat(resultUser).isNull();
    }

    @Test
    public void logout_callsSessionTokenService_expiresToken() throws Exception {
        userSessionsService.logout("test@email.com");

        verify(sessionTokenService).expire("test@email.com");
    }
}