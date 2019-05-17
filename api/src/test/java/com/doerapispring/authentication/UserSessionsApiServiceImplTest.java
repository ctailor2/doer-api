package com.doerapispring.authentication;

import com.doerapispring.domain.UserAlreadyExistsException;
import com.doerapispring.domain.UserService;
import com.doerapispring.session.SessionToken;
import com.doerapispring.web.SessionTokenDTO;
import org.junit.Before;
import org.junit.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class UserSessionsApiServiceImplTest {
    private UserSessionsApiServiceImpl userSessionsApiServiceImpl;

    private UserService mockUserService;

    private AuthenticationTokenService mockAuthenticationTokenService;

    private BasicAuthenticationService mockBasicAuthenticationService;

    private TransientAccessToken transientAccessToken;
    private String accessToken;

    @Before
    public void setUp() throws Exception {
        mockUserService = mock(UserService.class);
        mockAuthenticationTokenService = mock(AuthenticationTokenService.class);
        mockBasicAuthenticationService = mock(BasicAuthenticationService.class);
        accessToken = "hereIsYourFunAccessToken";
        transientAccessToken = new SessionToken(null, accessToken, new Date());
        userSessionsApiServiceImpl = new UserSessionsApiServiceImpl(mockUserService, mockAuthenticationTokenService, mockBasicAuthenticationService);
    }

    @Test
    public void signup_createsUser_registersCredentials_grantsTransientAccessToken_returnsTheToken() throws Exception {
        when(mockAuthenticationTokenService.grant(any())).thenReturn(transientAccessToken);

        String identifier = "soUnique";
        String credentials = "soSecure";
        SessionTokenDTO sessionTokenDTO = userSessionsApiServiceImpl.signup(identifier, credentials);

        verify(mockUserService).create(identifier);
        verify(mockBasicAuthenticationService).registerCredentials(identifier, credentials);
        verify(mockAuthenticationTokenService).grant(identifier);
        assertThat(sessionTokenDTO.getToken()).isEqualTo(accessToken);
        assertThat(sessionTokenDTO.getExpiresAt()).isToday();
    }

    @Test
    public void signup_whenUserCreationRefused_deniesAccess() throws Exception {
        when(mockUserService.create(any())).thenThrow(new UserAlreadyExistsException());

        assertThatThrownBy(() -> userSessionsApiServiceImpl.signup("soUnique", "soSecure"))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("Access denied - null");

        verifyZeroInteractions(mockBasicAuthenticationService);
        verifyZeroInteractions(mockAuthenticationTokenService);
    }

    @Test
    public void signup_whenCredentialRegistrationFails_deniesAccess() throws Exception {
        doThrow(new CredentialsInvalidException()).when(mockBasicAuthenticationService).registerCredentials(any(), any());

        assertThatThrownBy(() -> userSessionsApiServiceImpl.signup("soUnique", "soSecure"))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("Access denied - null");

        verifyZeroInteractions(mockAuthenticationTokenService);
    }

    @Test
    public void signup_whenTokenRefused_deniesAccess() throws Exception {
        when(mockAuthenticationTokenService.grant(any())).thenThrow(new TokenRefusedException());

        assertThatThrownBy(() -> userSessionsApiServiceImpl.signup("soUnique", "soSecure"))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("Access denied - null");
    }

    @Test
    public void login_whenAuthenticationFails_deniesAccess() throws Exception {
        when(mockBasicAuthenticationService.authenticate(any(), any())).thenReturn(false);

        assertThatThrownBy(() -> userSessionsApiServiceImpl.login("test@email.com", "password"))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("Access denied - unable to authenticate with the supplied credentials");

        verifyZeroInteractions(mockAuthenticationTokenService);
    }

    @Test
    public void login_authenticates_grantsTransientAccessToken_returnsTheToken() throws Exception {
        when(mockBasicAuthenticationService.authenticate(any(), any())).thenReturn(true);
        when(mockAuthenticationTokenService.grant(any())).thenReturn(transientAccessToken);

        String identifier = "test@email.com";
        String credentials = "password";
        SessionTokenDTO sessionTokenDTO = userSessionsApiServiceImpl.login(identifier, credentials);

        verify(mockBasicAuthenticationService).authenticate(identifier, credentials);
        verify(mockAuthenticationTokenService).grant(identifier);
        assertThat(sessionTokenDTO.getToken()).isEqualTo(accessToken);
        assertThat(sessionTokenDTO.getExpiresAt()).isToday();
    }

    @Test
    public void login_whenTokenRefused_deniesAccess() throws Exception {
        when(mockBasicAuthenticationService.authenticate(any(), any())).thenReturn(true);
        when(mockAuthenticationTokenService.grant(any())).thenThrow(new TokenRefusedException());

        String identifier = "test@email.com";
        String credentials = "password";
        assertThatThrownBy(() -> userSessionsApiServiceImpl.login(identifier, credentials))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("Access denied - null");
    }
}