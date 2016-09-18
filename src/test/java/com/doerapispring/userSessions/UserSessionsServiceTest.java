package com.doerapispring.userSessions;

import com.doerapispring.apiTokens.SessionToken;
import com.doerapispring.apiTokens.SessionTokenService;
import com.doerapispring.users.User;
import com.doerapispring.users.UserEntity;
import com.doerapispring.users.UserService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.anyString;
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
    private SessionToken sessionToken;
    private User user;

    @Before
    public void setUp() throws Exception {
        userSessionsService = new UserSessionsService(userService, sessionTokenService, authenticationService);

        userEntity = UserEntity.builder()
                .email("test@email.com")
                .password("password")
                .build();

        user = User.builder()
                .id(1L)
                .email("test@email.com")
                .passwordDigest("passwordDigest")
                .build();
        sessionToken = SessionToken.builder()
                .token("superSecureToken")
                .build();
    }

    @Test
    public void signup_callsUserService_callsSessionTokenService_returnsUser() throws Exception {
        UserEntity userEntity = UserEntity.builder().build();

        doReturn(user).when(userService).create(userEntity);
        doReturn(sessionToken).when(sessionTokenService).create(1L);

        UserEntity resultUserEntity = userSessionsService.signup(userEntity);

        verify(userService).create(userEntity);
        verify(sessionTokenService).create(1L);

        assertThat(resultUserEntity.getEmail()).isEqualTo("test@email.com");
        assertThat(resultUserEntity.getSessionToken().getToken()).isEqualTo("superSecureToken");
    }

    @Test
    public void login_callsUserService_whenUserWithEmailExists_callsAuthenticationService() throws Exception {
        doReturn(user).when(userService).get("test@email.com");

        userSessionsService.login(userEntity);

        verify(userService).get("test@email.com");
        verify(authenticationService).authenticatePassword("password", "passwordDigest");
    }

    @Test
    public void login_callsUserService_whenUserWithEmailDoesNotExist_doesNotCallAuthenticationService_returnsNull() throws Exception {
        doReturn(null).when(userService).get("test@email.com");

        UserEntity resultUserEntity = userSessionsService.login(userEntity);

        verify(userService).get("test@email.com");
        verifyZeroInteractions(authenticationService);

        assertThat(resultUserEntity).isNull();
    }

    @Test
    public void login_whenAuthenticationSuccessful_getsTokenFromSessionTokenService_returnsUser() throws Exception {
        doReturn(user).when(userService).get("test@email.com");
        doReturn(true).when(authenticationService).authenticatePassword(anyString(), anyString());
        doReturn(sessionToken).when(sessionTokenService).getActive(1L);

        UserEntity resultUserEntity = userSessionsService.login(userEntity);

        verify(sessionTokenService).getActive(1L);

        assertThat(resultUserEntity.getEmail()).isEqualTo("test@email.com");
        assertThat(resultUserEntity.getSessionToken().getToken()).isEqualTo("superSecureToken");
    }

    @Test
    public void login_whenAuthenticationSuccessful_whenTokenDoesNotExist_createsTokenWithSessionTokenService_returnsUser() throws Exception {
        doReturn(user).when(userService).get("test@email.com");
        doReturn(true).when(authenticationService).authenticatePassword(anyString(), anyString());
        doReturn(null).when(sessionTokenService).getActive(1L);
        doReturn(sessionToken).when(sessionTokenService).create(1L);

        UserEntity resultUserEntity = userSessionsService.login(userEntity);

        verify(sessionTokenService).getActive(1L);
        verify(sessionTokenService).create(1L);

        assertThat(resultUserEntity.getEmail()).isEqualTo("test@email.com");
        assertThat(resultUserEntity.getSessionToken().getToken()).isEqualTo("superSecureToken");
    }

    @Test
    public void login_whenAuthenticationFails_doesNotCallSessionTokenService_returnsNull() throws Exception {
        doReturn(user).when(userService).get("test@email.com");
        doReturn(false).when(authenticationService).authenticatePassword(anyString(), anyString());

        UserEntity resultUserEntity = userSessionsService.login(userEntity);

        verifyZeroInteractions(sessionTokenService);

        assertThat(resultUserEntity).isNull();
    }
}