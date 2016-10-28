package com.doerapispring.apiTokens;

import com.doerapispring.users.RegisteredUser;
import com.doerapispring.users.UserEntity;
import com.doerapispring.users.UserRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
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

    @Mock
    private NewSessionTokenRepository newSessionTokenRepository;

    private ArgumentCaptor<SessionTokenEntity> sessionTokenArgumentCaptor = ArgumentCaptor.forClass(SessionTokenEntity.class);

    @Captor
    private ArgumentCaptor<UserSession> userSessionArgumentCaptor;

    @Before
    public void setUp() throws Exception {
        sessionTokenService = new SessionTokenService(sessionTokenRepository,
                tokenGenerator,
                userRepository,
                newSessionTokenRepository);
    }

    @Test
    public void create_callsUserRepository_whenUserExists_callsTokenGenerator_callsSessionTokenRepository_setsFields_returnsSessionTokenEntity() throws Exception {
        UserEntity userEntity = UserEntity.builder().build();
        doReturn(userEntity).when(userRepository).findByEmail("email");
        doReturn("sorandomtoken").when(tokenGenerator).generate();

        SessionToken sessionToken = sessionTokenService.create("email");

        verify(userRepository).findByEmail("email");
        verify(tokenGenerator).generate();
        verify(sessionTokenRepository).save(sessionTokenArgumentCaptor.capture());
        SessionTokenEntity savedSessionTokenEntity = sessionTokenArgumentCaptor.getValue();
        assertThat(savedSessionTokenEntity.userEntity).isEqualTo(userEntity);
        assertThat(savedSessionTokenEntity.token).isEqualTo("sorandomtoken");
        assertThat(savedSessionTokenEntity.expiresAt).isInTheFuture();
        assertThat(savedSessionTokenEntity.createdAt).isToday();
        assertThat(savedSessionTokenEntity.updatedAt).isToday();
        assertThat(sessionToken.getToken()).isEqualTo("sorandomtoken");
    }

    @Test
    public void create_callsUserRepository_whenUserDoesNotExist_returnsNull() throws Exception {
        doReturn(null).when(userRepository).findByEmail("email");

        SessionToken sessionToken = sessionTokenService.create("email");

        assertThat(sessionToken).isNull();
    }

    @Test
    public void getActive_callsSessionTokenRepository_whenSessionTokenExists_returnsSessionTokenEntity() throws Exception {
        SessionTokenEntity sessionTokenEntity = SessionTokenEntity.builder()
                .token("zomgsecrets")
                .build();
        doReturn(sessionTokenEntity).when(sessionTokenRepository).findActiveByUserEmail("cool@email.com");

        SessionToken sessionToken = sessionTokenService.getActive("cool@email.com");

        verify(sessionTokenRepository).findActiveByUserEmail("cool@email.com");
        assertThat(sessionToken.getToken()).isEqualTo("zomgsecrets");
    }

    @Test
    public void getActive_callsSessionTokenRepository_whenSessionTokenDoesNotExist_returnsNull() throws Exception {
        doReturn(null).when(sessionTokenRepository).findActiveByUserEmail("cool@email.com");

        SessionToken sessionToken = sessionTokenService.getActive("cool@email.com");

        verify(sessionTokenRepository).findActiveByUserEmail("cool@email.com");
        assertThat(sessionToken).isNull();
    }

    @Test
    public void getByToken_callsSessionTokenRepository() throws Exception {
        sessionTokenService.getByToken("token");

        verify(sessionTokenRepository).findFirstByTokenAndExpiresAtAfter(eq("token"), any(Date.class));
    }

    @Test
    public void expire_callsUserRepository_whenUserExists_setsExpiredAt() throws Exception {
        SessionTokenEntity sessionTokenEntity = SessionTokenEntity.builder().build();
        doReturn(sessionTokenEntity).when(sessionTokenRepository).findActiveByUserEmail("test@email.com");

        sessionTokenService.expire("test@email.com");

        verify(sessionTokenRepository).findActiveByUserEmail("test@email.com");
        verify(sessionTokenRepository).save(sessionTokenArgumentCaptor.capture());
        SessionTokenEntity savedSessionTokenEntity = sessionTokenArgumentCaptor.getValue();
        assertThat(savedSessionTokenEntity.expiresAt).isToday();
    }

    @Test
    public void expire_callsUserRepository_whenUserDoesNotExist_doesNothing() throws Exception {
        doReturn(null).when(sessionTokenRepository).findActiveByUserEmail("test@email.com");

        sessionTokenService.expire("test@email.com");

        verify(sessionTokenRepository).findActiveByUserEmail("test@email.com");
        verifyNoMoreInteractions(sessionTokenRepository);
    }

    @Test
    public void start_callsTokenGenerator_createsUserSessionWithToken_addsToRepository() throws Exception {
        when(tokenGenerator.generate()).thenReturn("suchARandomTokenWow");
        RegisteredUser registeredUser = new RegisteredUser("test@email.com", "encodedPassword");

        sessionTokenService.start(registeredUser);

        verify(tokenGenerator).generate();
        verify(newSessionTokenRepository).add(userSessionArgumentCaptor.capture());
        UserSession userSession = userSessionArgumentCaptor.getValue();
        assertThat(userSession).isNotNull();
        assertThat(userSession.getEmail()).isEqualTo("test@email.com");
        assertThat(userSession.getToken()).isEqualTo("suchARandomTokenWow");
        assertThat(userSession.getExpiresAt()).isInTheFuture();
    }
}