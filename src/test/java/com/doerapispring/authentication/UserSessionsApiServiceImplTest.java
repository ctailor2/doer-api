package com.doerapispring.authentication;

import com.doerapispring.api.UserSessionsApiServiceImpl;
import com.doerapispring.domain.OperationRefusedException;
import com.doerapispring.domain.UserService;
import com.doerapispring.web.SessionTokenDTO;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
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
        when(mockUserService.create(any())).thenThrow(new OperationRefusedException());

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
        assertThatThrownBy(() -> userSessionsApiServiceImpl.login(identifier, credentials))
            .isInstanceOf(AccessDeniedException.class)
            .hasMessage("Access denied - null");
    }
}