package com.doerapispring.userSessions;

import com.doerapispring.apiTokens.SessionToken;
import com.doerapispring.apiTokens.SessionTokenService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

/**
 * Created by chiragtailor on 8/29/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthenticationServiceTest {
    private AuthenticationService authenticationService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private SessionTokenService sessionTokenService;

    @Before
    public void setUp() throws Exception {
        authenticationService = new AuthenticationService(passwordEncoder, sessionTokenService);
    }

    @Test
    public void authenticatePassword_callsPasswordEncoder_returnsFalse_whenPasswordsDoNotMatch() throws Exception {
        doReturn(false).when(passwordEncoder).matches(anyString(), anyString());
        boolean result = authenticationService.authenticatePassword("cool", "beans");
        verify(passwordEncoder).matches("cool", "beans");
        assertThat(result).isEqualTo(false);
    }

    @Test
    public void authenticatePassword_callsPasswordEncoder_returnsTrue_whenPasswordsMatch() throws Exception {
        doReturn(true).when(passwordEncoder).matches(anyString(), anyString());
        boolean result = authenticationService.authenticatePassword("cool", "beans");
        verify(passwordEncoder).matches("cool", "beans");
        assertThat(result).isEqualTo(true);
    }

    @Test
    public void authenticateSessionToken_callsSessionTokenService_returnsTrue_whenSessionTokenFound() throws Exception {
        doReturn(SessionToken.builder().build()).when(sessionTokenService).getByToken("token");

        boolean result = authenticationService.authenticateSessionToken("token");

        verify(sessionTokenService).getByToken("token");
        assertThat(result).isEqualTo(true);
    }

    @Test
    public void authenticateSessionToken_callsSessionTokenService_returnsFalse_whenSessionTokenNotFound() throws Exception {
        doReturn(null).when(sessionTokenService).getByToken("token");

        boolean result = authenticationService.authenticateSessionToken("token");

        verify(sessionTokenService).getByToken("token");
        assertThat(result).isEqualTo(false);
    }
}