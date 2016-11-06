package com.doerapispring.userSessions;

import com.doerapispring.Credentials;
import com.doerapispring.UserIdentifier;
import com.doerapispring.apiTokens.SessionToken;
import com.doerapispring.apiTokens.SessionTokenService;
import com.doerapispring.users.NewUser;
import com.doerapispring.users.User;
import com.doerapispring.users.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

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
    public void signup_createsUser_whenSuccessful_registersCredentialsForUser_grantsAccessToken_andReturnsIt() throws Exception {
        UserIdentifier userIdentifier = new UserIdentifier("soUnique");
        Credentials credentials = new Credentials("soSecure");

        when(userService.create(any())).thenReturn(new NewUser(userIdentifier));
        when(sessionTokenService.grant(any()))
                .thenReturn(SessionToken.builder().build());

        SessionToken sessionToken = userSessionsService.signup(userIdentifier, credentials);

        verify(userService).create(userIdentifier);
        verify(authenticationService).registerCredentials(userIdentifier, credentials);
        verify(sessionTokenService).grant(userIdentifier);
        assertThat(sessionToken).isNotNull();
    }

    @Test
    public void signup_createsUser_whenFailed_doesNotRegisterCredentialsForUser_returnsNull() throws Exception {
        UserIdentifier userIdentifier = new UserIdentifier("soUnique");
        Credentials credentials = new Credentials("soSecure");

        when(userService.create(any())).thenReturn(null);

        SessionToken sessionToken = userSessionsService.signup(userIdentifier, credentials);

        verify(userService).create(userIdentifier);
        verifyZeroInteractions(authenticationService);
        assertThat(sessionToken).isNull();
    }

    @Test
    public void newLogin_callsAuthenticationService_whenAuthenticationSuccessful_callsSessionTokenService() throws Exception {
        when(authenticationService.authenticate(any(UserIdentifier.class),
                any(Credentials.class))).thenReturn(true);

        UserIdentifier userIdentifier = new UserIdentifier("test@email.com");
        Credentials credentials = new Credentials("password");
        userSessionsService.login(userIdentifier, credentials);

        verify(authenticationService).authenticate(userIdentifier, credentials);
        verify(sessionTokenService).grant(userIdentifier);
    }

    @Test
    public void logout_callsSessionTokenService_expiresToken() throws Exception {
        userSessionsService.logout("test@email.com");

        verify(sessionTokenService).expire("test@email.com");
    }
}