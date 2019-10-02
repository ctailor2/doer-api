package com.doerapispring.authentication;

import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class AuthenticationServiceTest {
    private AuthenticationService authenticationService;

    private PasswordEncoder passwordEncoder;

    private CredentialsStore credentialsStore;

    private final ArgumentCaptor<Credentials> credentialsArgumentCaptor = ArgumentCaptor.forClass(Credentials.class);

    @Before
    public void setUp() throws Exception {
        passwordEncoder = mock(PasswordEncoder.class);
        credentialsStore = mock(CredentialsStore.class);
        authenticationService = new AuthenticationService(
            passwordEncoder,
            credentialsStore);
    }

    @Test
    public void registerCredentials_callsPasswordEncoder_callsUserCredentialsRepository() throws Exception {
        String userIdentifier = "someId";
        String credentials = "soSecret";
        String encodedCredentials = "encodedSecretPassword";
        when(passwordEncoder.encode(any())).thenReturn(encodedCredentials);
        authenticationService.registerCredentials(userIdentifier, credentials);

        verify(passwordEncoder).encode(credentials);
        verify(credentialsStore).add(credentialsArgumentCaptor.capture());
        Credentials addedCredentials = credentialsArgumentCaptor.getValue();
        assertThat(addedCredentials.getUserIdentifier()).isEqualTo(userIdentifier);
        assertThat(addedCredentials.getSecret()).isEqualTo(encodedCredentials);
    }

    @Test
    public void authenticate_whenUserCredentialsExist_callsPasswordEncoder() throws Exception {
        String userIdentifier = "someId";
        Credentials credentials = new Credentials(userIdentifier, "encodedSecretPassword", new Date());
        when(credentialsStore.findLatest(any())).thenReturn(Optional.of(credentials));

        authenticationService.authenticate(userIdentifier, "soSecret");

        verify(credentialsStore).findLatest(userIdentifier);
        verify(passwordEncoder).matches("soSecret", "encodedSecretPassword");
    }

    @Test
    public void authenticate_whenUserCredentialsDoNotExist_returnsFalse() throws Exception {
        when(credentialsStore.findLatest(any())).thenReturn(Optional.empty());

        String userIdentifier = "someId";
        boolean authenticationResult = authenticationService.authenticate(userIdentifier, "soSecret");

        verify(credentialsStore).findLatest(userIdentifier);
        verifyZeroInteractions(passwordEncoder);
        assertThat(authenticationResult).isFalse();
    }
}