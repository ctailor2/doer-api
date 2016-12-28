package com.doerapispring.authentication;

import com.doerapispring.domain.OperationRefusedException;
import com.doerapispring.domain.UniqueIdentifier;
import com.doerapispring.domain.User;
import com.doerapispring.domain.UserService;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserSessionsServiceTest {
    private UserSessionsService userSessionsService;

    @Mock
    private UserService userService;

    @Mock
    private SessionTokenService sessionTokenService;

    @Mock
    private AuthenticationService authenticationService;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        userSessionsService = new UserSessionsService(userService, sessionTokenService, authenticationService);
    }

    @Test
    public void signup_createsUser_whenSuccessful_registersCredentialsForUser_grantsAccessToken_andReturnsIt() throws Exception {
        UniqueIdentifier uniqueIdentifier = new UniqueIdentifier("soUnique");
        Credentials credentials = new Credentials("soSecure");

        when(userService.create(any())).thenReturn(new User(uniqueIdentifier));
        when(sessionTokenService.grant(any()))
                .thenReturn(SessionToken.builder().build());

        SessionToken sessionToken = userSessionsService.signup(uniqueIdentifier, credentials);

        verify(userService).create(uniqueIdentifier);
        verify(authenticationService).registerCredentials(uniqueIdentifier, credentials);
        verify(sessionTokenService).grant(uniqueIdentifier);
        assertThat(sessionToken).isNotNull();
    }

    @Test
    public void signup_callsUserService() throws Exception {
        UniqueIdentifier uniqueIdentifier = new UniqueIdentifier("soUnique");
        userSessionsService.signup(uniqueIdentifier, new Credentials("soSecure"));

        verify(userService).create(uniqueIdentifier);
    }

    @Test
    public void signup_whenUserCreated_registersUserCredentials() throws Exception {
        when(userService.create(any())).thenReturn(new User(new UniqueIdentifier("something")));

        UniqueIdentifier uniqueIdentifier = new UniqueIdentifier("soUnique");
        Credentials credentials = new Credentials("soSecure");
        userSessionsService.signup(uniqueIdentifier, credentials);

        verify(authenticationService).registerCredentials(uniqueIdentifier, credentials);
    }

    @Test
    public void signup_whenUserCreationRefused_refusesSignup() throws Exception {
        when(userService.create(any())).thenThrow(OperationRefusedException.class);

        exception.expect(OperationRefusedException.class);
        userSessionsService.signup(new UniqueIdentifier("soUnique"), new Credentials("soSecure"));

        verifyZeroInteractions(authenticationService);
        verifyZeroInteractions(sessionTokenService);
    }

    @Test
    public void signup_whenCredentialsRegistered_grantsSession() throws Exception {
        when(userService.create(any())).thenReturn(new User(new UniqueIdentifier("something")));

        UniqueIdentifier uniqueIdentifier = new UniqueIdentifier("soUnique");
        userSessionsService.signup(uniqueIdentifier, new Credentials("soSecure"));

        verify(sessionTokenService).grant(uniqueIdentifier);
    }

    @Test
    public void signup_whenCredentialRegistrationRefused_refusesSignup() throws Exception {
        when(userService.create(any())).thenReturn(new User(new UniqueIdentifier("something")));
        doThrow(OperationRefusedException.class).when(authenticationService).registerCredentials(any(), any());

        exception.expect(OperationRefusedException.class);
        userSessionsService.signup(new UniqueIdentifier("soUnique"), new Credentials("soSecure"));

        verifyZeroInteractions(sessionTokenService);
    }

    @Test
    public void signup_whenSessionGrantRefused_refusesSignup() throws Exception {
        when(userService.create(any())).thenReturn(new User(new UniqueIdentifier("something")));
        when(sessionTokenService.grant(any())).thenThrow(OperationRefusedException.class);

        exception.expect(OperationRefusedException.class);
        userSessionsService.signup(new UniqueIdentifier("soUnique"), new Credentials("soSecure"));
    }

    @Test
    public void login_callsAuthenticationService_whenAuthenticationSuccessful_callsSessionTokenService() throws Exception {
        when(authenticationService.authenticate(any(UniqueIdentifier.class),
                any(Credentials.class))).thenReturn(true);

        UniqueIdentifier uniqueIdentifier = new UniqueIdentifier("test@email.com");
        Credentials credentials = new Credentials("password");
        userSessionsService.login(uniqueIdentifier, credentials);

        verify(authenticationService).authenticate(uniqueIdentifier, credentials);
    }

    @Test
    public void login_whenAuthenticationFails_deniesAccess() throws Exception {
        when(authenticationService.authenticate(any(UniqueIdentifier.class),
                any(Credentials.class))).thenReturn(false);

        exception.expect(AccessDeniedException.class);
        userSessionsService.login(new UniqueIdentifier("test@email.com"), new Credentials("password"));

        verifyZeroInteractions(sessionTokenService);
    }

    @Test
    public void login_whenAuthenticationSuccessful_grantsSession() throws Exception {
        when(authenticationService.authenticate(any(UniqueIdentifier.class),
                any(Credentials.class))).thenReturn(true);

        UniqueIdentifier uniqueIdentifier = new UniqueIdentifier("test@email.com");
        userSessionsService.login(uniqueIdentifier, new Credentials("password"));

        verify(sessionTokenService).grant(uniqueIdentifier);
    }

    @Test
    public void login_whenSessionTokenGrantRefused_deniesAccess() throws Exception {
        when(authenticationService.authenticate(any(UniqueIdentifier.class),
                any(Credentials.class))).thenReturn(true);
        when(sessionTokenService.grant(any())).thenThrow(OperationRefusedException.class);

        exception.expect(AccessDeniedException.class);
        userSessionsService.login(new UniqueIdentifier("test@email.com"), new Credentials("password"));
    }
}