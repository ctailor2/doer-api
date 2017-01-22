package com.doerapispring.authentication;

import com.doerapispring.api.UserSessionsApiServiceImpl;
import com.doerapispring.domain.OperationRefusedException;
import com.doerapispring.domain.UserService;
import com.doerapispring.web.SessionTokenDTO;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserSessionsApiServiceImplTest {
    private UserSessionsApiServiceImpl userSessionsApiServiceImpl;

    @Mock
    private UserService mockUserService;

    @Mock
    private AuthenticationTokenService mockAuthenticationTokenService;

    @Mock
    private BasicAuthenticationService mockBasicAuthenticationService;

    @Mock
    private TransientAccessToken mockTransientAccessToken;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        String accessToken = "hereIsYourFunAccessToken";
        when(mockTransientAccessToken.getAccessToken()).thenReturn(accessToken);
        Date expiresAt = new Date();
        when(mockTransientAccessToken.getExpiresAt()).thenReturn(expiresAt);
        userSessionsApiServiceImpl = new UserSessionsApiServiceImpl(mockUserService, mockAuthenticationTokenService, mockBasicAuthenticationService);
    }

    @Test
    public void signup_createsUser_registersCredentials_grantsTransientAccessToken_returnsTheToken() throws Exception {
        when(mockAuthenticationTokenService.grant(any())).thenReturn(mockTransientAccessToken);

        String identifier = "soUnique";
        String credentials = "soSecure";
        SessionTokenDTO sessionTokenDTO = userSessionsApiServiceImpl.signup(identifier, credentials);

        verify(mockUserService).create(identifier);
        verify(mockBasicAuthenticationService).registerCredentials(identifier, credentials);
        verify(mockAuthenticationTokenService).grant(identifier);
        assertThat(sessionTokenDTO.getToken()).isEqualTo("hereIsYourFunAccessToken");
        assertThat(sessionTokenDTO.getExpiresAt()).isToday();
    }

    @Test
    public void signup_whenUserCreationRefused_deniesAccess() throws Exception {
        when(mockUserService.create(any())).thenThrow(OperationRefusedException.class);

        exception.expect(AccessDeniedException.class);
        userSessionsApiServiceImpl.signup("soUnique", "soSecure");

        verifyZeroInteractions(mockBasicAuthenticationService);
        verifyZeroInteractions(mockAuthenticationTokenService);
    }

    @Test
    public void signup_whenCredentialRegistrationFails_deniesAccess() throws Exception {
        doThrow(CredentialsInvalidException.class).when(mockBasicAuthenticationService).registerCredentials(any(), any());

        exception.expect(AccessDeniedException.class);
        userSessionsApiServiceImpl.signup("soUnique", "soSecure");

        verifyZeroInteractions(mockAuthenticationTokenService);
    }

    @Test
    public void signup_whenTokenRefused_deniesAccess() throws Exception {
        when(mockAuthenticationTokenService.grant(any())).thenThrow(new TokenRefusedException());

        exception.expect(AccessDeniedException.class);
        userSessionsApiServiceImpl.signup("soUnique", "soSecure");
    }

    @Test
    public void login_whenAuthenticationFails_deniesAccess() throws Exception {
        when(mockBasicAuthenticationService.authenticate(any(), any())).thenReturn(false);

        exception.expect(AccessDeniedException.class);
        userSessionsApiServiceImpl.login("test@email.com", "password");

        verifyZeroInteractions(mockAuthenticationTokenService);
    }

    @Test
    public void login_authenticates_grantsTransientAccessToken_returnsTheToken() throws Exception {
        when(mockBasicAuthenticationService.authenticate(any(), any())).thenReturn(true);
        when(mockAuthenticationTokenService.grant(any())).thenReturn(mockTransientAccessToken);

        String identifier = "test@email.com";
        String credentials = "password";
        SessionTokenDTO sessionTokenDTO = userSessionsApiServiceImpl.login(identifier, credentials);

        verify(mockBasicAuthenticationService).authenticate(identifier, credentials);
        verify(mockAuthenticationTokenService).grant(identifier);
        assertThat(sessionTokenDTO.getToken()).isEqualTo("hereIsYourFunAccessToken");
        assertThat(sessionTokenDTO.getExpiresAt()).isToday();
    }

    @Test
    public void login_whenTokenRefused_deniesAccess() throws Exception {
        when(mockBasicAuthenticationService.authenticate(any(), any())).thenReturn(true);
        when(mockAuthenticationTokenService.grant(any())).thenThrow(new TokenRefusedException());

        String identifier = "test@email.com";
        String credentials = "password";
        exception.expect(AccessDeniedException.class);
        userSessionsApiServiceImpl.login(identifier, credentials);
    }
}