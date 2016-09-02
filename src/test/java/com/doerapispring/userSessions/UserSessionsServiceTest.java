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
        assertThat(resultUserEntity.getSessionTokenEntity().getToken()).isEqualTo("superSecureToken");
    }

    @Test
    public void login_callsUserService_callsAuthenticationService() throws Exception {
        doReturn(user).when(userService).get("test@email.com");
        doReturn(sessionToken).when(sessionTokenService).create(1L);

        userSessionsService.login(userEntity);

        verify(userService).get("test@email.com");
        verify(authenticationService).authenticate("password", "passwordDigest");
    }

    @Test
    public void login_callsSessionTokenService_whenAuthenticationSuccessful_returnsUser() throws Exception {
        doReturn(user).when(userService).get("test@email.com");
        doReturn(true).when(authenticationService).authenticate(anyString(), anyString());
        doReturn(sessionToken).when(sessionTokenService).create(1L);

        UserEntity resultUserEntity = userSessionsService.login(userEntity);

        verify(sessionTokenService).create(1L);

        assertThat(resultUserEntity.getEmail()).isEqualTo("test@email.com");
        assertThat(resultUserEntity.getSessionTokenEntity().getToken()).isEqualTo("superSecureToken");
    }

    @Test
    public void login_doesNotCallSessionTokenService_whenAuthenticationFails_returnsNull() throws Exception {
        doReturn(user).when(userService).get("test@email.com");
        doReturn(false).when(authenticationService).authenticate(anyString(), anyString());

        UserEntity resultUserEntity = userSessionsService.login(userEntity);

        verifyZeroInteractions(sessionTokenService);

        assertThat(resultUserEntity).isNull();
    }
}