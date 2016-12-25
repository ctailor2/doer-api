package com.doerapispring.authentication;

import com.doerapispring.domain.ObjectRepository;
import com.doerapispring.domain.UserIdentifier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class AuthenticationServiceTest {
    private AuthenticationService authenticationService;

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private ObjectRepository<UserCredentials, String> userCredentialsRepository;

    @Captor
    private ArgumentCaptor<UserCredentials> userCredentialsArgumentCaptor;

    @Before
    public void setUp() throws Exception {
        authenticationService = new AuthenticationService(passwordEncoder,
                userCredentialsRepository);
    }

    @Test
    public void registerCredentials_callsPasswordEncoder_callsUserCredentialsRepository() throws Exception {
        UserIdentifier userIdentifier = new UserIdentifier("someId");
        Credentials credentials = new Credentials("soSecret");
        when(passwordEncoder.encode(any())).thenReturn("encodedSecretPassword");
        authenticationService.registerCredentials(userIdentifier, credentials);

        verify(passwordEncoder).encode("soSecret");
        verify(userCredentialsRepository).add(userCredentialsArgumentCaptor.capture());
        UserCredentials userCredentials = userCredentialsArgumentCaptor.getValue();
        assertThat(userCredentials.getIdentifier()).isEqualTo(userIdentifier);
        assertThat(userCredentials.getEncodedCredentials())
                .isEqualTo(new EncodedCredentials("encodedSecretPassword"));
    }

    @Test
    public void authenticate_whenUserCredentialsExist_callsPasswordEncoder() throws Exception {
        UserIdentifier userIdentifier = new UserIdentifier("someId");
        UserCredentials userCredentials = new UserCredentials(
                userIdentifier,
                new EncodedCredentials("encodedSecretPassword"));
        when(userCredentialsRepository.find(any())).thenReturn(Optional.of(userCredentials));

        authenticationService.authenticate(userIdentifier, new Credentials("soSecret"));

        verify(userCredentialsRepository).find(userIdentifier);
        verify(passwordEncoder).matches("soSecret", "encodedSecretPassword");
    }

    @Test
    public void authenticate_whenUserCredentialsDoNotExist_returnsFalse() throws Exception {
        when(userCredentialsRepository.find(any())).thenReturn(Optional.empty());

        UserIdentifier userIdentifier = new UserIdentifier("someId");
        boolean authenticationResult = authenticationService.authenticate(
                userIdentifier,
                new Credentials("soSecret"));

        verify(userCredentialsRepository).find(userIdentifier);
        verifyZeroInteractions(passwordEncoder);
        assertThat(authenticationResult).isFalse();
    }
}