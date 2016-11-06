package com.doerapispring.userSessions;

import com.doerapispring.Credentials;
import com.doerapispring.UserIdentifier;
import com.doerapispring.apiTokens.SessionToken;
import com.doerapispring.apiTokens.SessionTokenService;
import com.doerapispring.apiTokens.UserSession;
import com.doerapispring.users.NewUser;
import com.doerapispring.users.RegisteredUser;
import com.doerapispring.users.User;
import com.doerapispring.users.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
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
    public void newSignup_callsUserService_callsSessionTokenService_returnsUser() throws Exception {
        RegisteredUser registeredUser = new RegisteredUser("test@email.com", "encodedPassword");
        when(userService.createRegisteredUser(any(), any())).thenReturn(registeredUser);
        UserSession userSession = new UserSession("test@email.com", "token", new Date());
        when(sessionTokenService.start(any())).thenReturn(userSession);

        User user = userSessionsService.newSignup("test@email.com", "password");

        verify(userService).createRegisteredUser("test@email.com", "password");
        verify(sessionTokenService).start(registeredUser);
        assertThat(user.getEmail()).isEqualTo("test@email.com");
        assertThat(user.getSessionToken().getToken()).isEqualTo("token");
        assertThat(user.getSessionToken().getExpiresAt()).isToday();
    }

    @Test
    public void newerSignup_createsUser_whenSuccessful_registersCredentialsForUser_grantsAccessToken_andReturnsIt() throws Exception {
        UserIdentifier userIdentifier = new UserIdentifier("soUnique");
        Credentials credentials = new Credentials("soSecure");

        when(userService.newCreate(any())).thenReturn(new NewUser(userIdentifier));
        when(sessionTokenService.grant(any()))
                .thenReturn(SessionToken.builder().build());

        SessionToken sessionToken = userSessionsService.newerSignup(userIdentifier, credentials);

        verify(userService).newCreate(userIdentifier);
        verify(authenticationService).registerCredentials(userIdentifier, credentials);
        verify(sessionTokenService).grant(userIdentifier);
        assertThat(sessionToken).isNotNull();
    }

    @Test
    public void newerSignup_createsUser_whenFailed_doesNotRegisterCredentialsForUser_returnsNull() throws Exception {
        UserIdentifier userIdentifier = new UserIdentifier("soUnique");
        Credentials credentials = new Credentials("soSecure");

        when(userService.newCreate(any())).thenReturn(null);

        SessionToken sessionToken = userSessionsService.newerSignup(userIdentifier, credentials);

        verify(userService).newCreate(userIdentifier);
        verifyZeroInteractions(authenticationService);
        assertThat(sessionToken).isNull();
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
    public void newLogin_callsUserService_whenUserExists() throws Exception {
        // TODO: Fill me out

    }

    @Test
    public void logout_callsSessionTokenService_expiresToken() throws Exception {
        userSessionsService.logout("test@email.com");

        verify(sessionTokenService).expire("test@email.com");
    }
}