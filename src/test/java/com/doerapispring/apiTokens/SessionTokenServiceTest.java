package com.doerapispring.apiTokens;

import com.doerapispring.users.User;
import com.doerapispring.users.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Created by chiragtailor on 8/25/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class SessionTokenServiceTest {
    private SessionTokenService sessionTokenService;

    @Mock
    private SessionTokenRepository sessionTokenRepository;

    @Mock
    private TokenGenerator tokenGenerator;

    @Mock
    private UserRepository userRepository;

    private ArgumentCaptor<SessionToken> sessionTokenArgumentCaptor = ArgumentCaptor.forClass(SessionToken.class);

    @Before
    public void setUp() throws Exception {
        sessionTokenService = new SessionTokenService(sessionTokenRepository, tokenGenerator, userRepository);
    }

    @Test
    public void create_callsUserRepository_whenUserExists_callsTokenGenerator_callsSessionTokenRepository_setsFields_returnsSessionTokenEntity() throws Exception {
        User user = User.builder().build();
        doReturn(user).when(userRepository).findByEmail("email");
        doReturn("sorandomtoken").when(tokenGenerator).generate();

        SessionTokenEntity sessionTokenEntity = sessionTokenService.create("email");

        verify(userRepository).findByEmail("email");
        verify(tokenGenerator).generate();
        verify(sessionTokenRepository).save(sessionTokenArgumentCaptor.capture());
        SessionToken savedSessionToken = sessionTokenArgumentCaptor.getValue();
        assertThat(savedSessionToken.user).isEqualTo(user);
        assertThat(savedSessionToken.token).isEqualTo("sorandomtoken");
        assertThat(savedSessionToken.expiresAt).isInTheFuture();
        assertThat(savedSessionToken.createdAt).isToday();
        assertThat(savedSessionToken.updatedAt).isToday();
        assertThat(sessionTokenEntity.getToken()).isEqualTo("sorandomtoken");
    }

    @Test
    public void create_callsUserRepository_whenUserDoesNotExist_returnsNull() throws Exception {
        doReturn(null).when(userRepository).findByEmail("email");

        SessionTokenEntity sessionTokenEntity = sessionTokenService.create("email");

        assertThat(sessionTokenEntity).isNull();
    }

    @Test
    public void getActive_callsSessionTokenRepository_whenSessionTokenExists_returnsSessionTokenEntity() throws Exception {
        SessionToken sessionToken = SessionToken.builder()
                .token("zomgsecrets")
                .build();
        doReturn(sessionToken).when(sessionTokenRepository).findActiveByUserEmail("cool@email.com");

        SessionTokenEntity sessionTokenEntity = sessionTokenService.getActive("cool@email.com");

        verify(sessionTokenRepository).findActiveByUserEmail("cool@email.com");
        assertThat(sessionTokenEntity.getToken()).isEqualTo("zomgsecrets");
    }

    @Test
    public void getActive_callsSessionTokenRepository_whenSessionTokenDoesNotExist_returnsNull() throws Exception {
        doReturn(null).when(sessionTokenRepository).findActiveByUserEmail("cool@email.com");

        SessionTokenEntity sessionTokenEntity = sessionTokenService.getActive("cool@email.com");

        verify(sessionTokenRepository).findActiveByUserEmail("cool@email.com");
        assertThat(sessionTokenEntity).isNull();
    }

    @Test
    public void getByToken_callsSessionTokenRepository() throws Exception {
        sessionTokenService.getByToken("token");

        verify(sessionTokenRepository).findFirstByTokenAndExpiresAtAfter(eq("token"), any(Date.class));
    }

    @Test
    public void expire_callsUserRepository_whenUserExists_setsExpiredAt() throws Exception {
        SessionToken sessionToken = SessionToken.builder().build();
        doReturn(sessionToken).when(sessionTokenRepository).findActiveByUserEmail("test@email.com");

        sessionTokenService.expire("test@email.com");

        verify(sessionTokenRepository).findActiveByUserEmail("test@email.com");
        verify(sessionTokenRepository).save(sessionTokenArgumentCaptor.capture());
        SessionToken savedSessionToken = sessionTokenArgumentCaptor.getValue();
        assertThat(savedSessionToken.expiresAt).isToday();
    }

    @Test
    public void expire_callsUserRepository_whenUserDoesNotExist_doesNothing() throws Exception {
        doReturn(null).when(sessionTokenRepository).findActiveByUserEmail("test@email.com");

        sessionTokenService.expire("test@email.com");

        verify(sessionTokenRepository).findActiveByUserEmail("test@email.com");
        verifyNoMoreInteractions(sessionTokenRepository);
    }
}