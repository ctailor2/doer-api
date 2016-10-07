package com.doerapispring.userSessions;

import com.doerapispring.apiTokens.SessionTokenEntity;
import com.doerapispring.apiTokens.SessionTokenService;
import com.doerapispring.users.UserEntity;
import com.doerapispring.users.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
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

    private UserEntity userEntity;
    private SessionTokenEntity sessionTokenEntity;

    @Before
    public void setUp() throws Exception {
        userSessionsService = new UserSessionsService(userService, sessionTokenService, authenticationService);

        userEntity = UserEntity.builder()
                .email("test@email.com")
                .password("password")
                .build();

        sessionTokenEntity = SessionTokenEntity.builder()
                .token("superSecureToken")
                .build();
    }

    @Test
    public void signup_callsUserService_callsSessionTokenService_returnsUser() throws Exception {
        doReturn(userEntity).when(userService).create(userEntity);
        doReturn(sessionTokenEntity).when(sessionTokenService).create("test@email.com");

        UserEntity resultUserEntity = userSessionsService.signup(userEntity);

        verify(userService).create(userEntity);
        verify(sessionTokenService).create("test@email.com");

        assertThat(resultUserEntity.getEmail()).isEqualTo("test@email.com");
        assertThat(resultUserEntity.getSessionToken().getToken()).isEqualTo("superSecureToken");
    }

    @Test
    public void login_callsAuthenticationService_whenAuthenticationSuccessful_getsTokenFromSessionTokenService_returnsUser() throws Exception {
        doReturn(true).when(authenticationService).authenticate("test@email.com", "password");
        doReturn(sessionTokenEntity).when(sessionTokenService).getActive("test@email.com");

        UserEntity resultUserEntity = userSessionsService.login(userEntity);

        verify(sessionTokenService).getActive("test@email.com");

        assertThat(resultUserEntity.getEmail()).isEqualTo("test@email.com");
        assertThat(resultUserEntity.getSessionToken().getToken()).isEqualTo("superSecureToken");
    }

    @Test
    public void login_callsAuthenticationService_whenAuthenticationSuccessful_whenTokenDoesNotExist_createsTokenWithSessionTokenService_returnsUser() throws Exception {
        doReturn(true).when(authenticationService).authenticate("test@email.com", "password");
        doReturn(null).when(sessionTokenService).getActive("test@email.com");
        doReturn(sessionTokenEntity).when(sessionTokenService).create("test@email.com");

        UserEntity resultUserEntity = userSessionsService.login(userEntity);

        verify(authenticationService).authenticate("test@email.com", "password");
        verify(sessionTokenService).getActive("test@email.com");
        verify(sessionTokenService).create("test@email.com");

        assertThat(resultUserEntity.getEmail()).isEqualTo("test@email.com");
        assertThat(resultUserEntity.getSessionToken().getToken()).isEqualTo("superSecureToken");
    }

    @Test
    public void login_callsAuthenticationService_whenAuthenticationFails_doesNotCallSessionTokenService_returnsNull() throws Exception {
        doReturn(false).when(authenticationService).authenticate("test@email.com", "password");

        UserEntity resultUserEntity = userSessionsService.login(userEntity);

        verify(authenticationService).authenticate("test@email.com", "password");
        verifyZeroInteractions(sessionTokenService);

        assertThat(resultUserEntity).isNull();
    }

    @Test
    public void logout_callsSessionTokenService_expiresToken() throws Exception {
        userSessionsService.logout("test@email.com");

        verify(sessionTokenService).expire("test@email.com");
    }
}