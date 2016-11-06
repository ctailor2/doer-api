package com.doerapispring.userSessions;

import com.doerapispring.*;
import com.doerapispring.users.UserEntity;
import com.doerapispring.users.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by chiragtailor on 8/29/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class AuthenticationServiceTest {
    private AuthenticationService authenticationService;

    private UserEntity userEntity;

    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserCredentialsRepository userCredentialsRepository;

    @Captor
    private ArgumentCaptor<UserCredentials> userCredentialsArgumentCaptor;

    @Before
    public void setUp() throws Exception {
        authenticationService = new AuthenticationService(passwordEncoder,
                userRepository,
                userCredentialsRepository);
        userEntity = UserEntity.builder()
                .email("wow@email.com")
                .passwordDigest("beans")
                .build();
    }

    @Test
    public void authenticate_callsUserRepository_whenUserExists_callsPasswordEncoder_returnsFalse_whenPasswordsDoNotMatch() throws Exception {
        doReturn(userEntity).when(userRepository).findByEmail("wow@email.com");
        doReturn(false).when(passwordEncoder).matches(anyString(), anyString());
        boolean result = authenticationService.authenticate("wow@email.com", "cool");
        verify(userRepository).findByEmail("wow@email.com");
        verify(passwordEncoder).matches("cool", "beans");
        assertThat(result).isEqualTo(false);
    }

    @Test
    public void authenticate_callsUserRepository_whenUserExists_callsPasswordEncoder_returnsTrue_whenPasswordsMatch() throws Exception {
        doReturn(userEntity).when(userRepository).findByEmail("wow@email.com");
        doReturn(true).when(passwordEncoder).matches(anyString(), anyString());
        boolean result = authenticationService.authenticate("wow@email.com", "cool");
        verify(userRepository).findByEmail("wow@email.com");
        verify(passwordEncoder).matches("cool", "beans");
        assertThat(result).isEqualTo(true);
    }

    @Test
    public void authenticate_callsUserRepository_whenUserDoesNotExist_returnsFalse() throws Exception {
        doReturn(null).when(userRepository).findByEmail("wow@email.com");
        boolean result = authenticationService.authenticate("wow@email.com", "cool");
        verify(userRepository).findByEmail("wow@email.com");
        assertThat(result).isEqualTo(false);
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
        assertThat(userCredentials.getUserIdentifier()).isEqualTo(userIdentifier);
        assertThat(userCredentials.getEncodedCredentials())
                .isEqualTo(new EncodedCredentials("encodedSecretPassword"));
    }
}