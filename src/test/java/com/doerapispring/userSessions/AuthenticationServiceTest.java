package com.doerapispring.userSessions;

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

    @Before
    public void setUp() throws Exception {
        authenticationService = new AuthenticationService(passwordEncoder);
    }

    @Test
    public void authenticate_callsPasswordEncoder_returnsFalse_whenPasswordsDoNotMatch() throws Exception {
        doReturn(false).when(passwordEncoder).matches(anyString(), anyString());
        boolean result = authenticationService.authenticate("cool", "beans");
        verify(passwordEncoder).matches("cool", "beans");
        assertThat(result).isEqualTo(false);
    }

    @Test
    public void authenticate_callsPasswordEncoder_returnsTrue_whenPasswordsMatch() throws Exception {
        doReturn(true).when(passwordEncoder).matches(anyString(), anyString());
        boolean result = authenticationService.authenticate("cool", "beans");
        verify(passwordEncoder).matches("cool", "beans");
        assertThat(result).isEqualTo(true);
    }
}