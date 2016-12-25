package com.doerapispring.storage;

import com.doerapispring.authentication.SessionToken;
import com.doerapispring.authentication.SessionTokenIdentifier;
import com.doerapispring.domain.AbnormalModelException;
import com.doerapispring.domain.ObjectRepository;
import com.doerapispring.domain.UserIdentifier;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;
import java.util.Optional;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class SessionTokenRepositoryTest {
    private ObjectRepository<SessionToken, String> sessionTokenRepository;

    @Mock
    private UserDAO userDAO;

    @Mock
    private SessionTokenDAO sessionTokenDAO;

    @Captor
    ArgumentCaptor<SessionTokenEntity> sessionTokenEntityArgumentCaptor;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        sessionTokenRepository = new SessionTokenRepository(userDAO, sessionTokenDAO);
    }

    @Test
    public void add_sessionToken_findsUser_whenFound_savesRelationship_savesFields_setsAuditingData() throws Exception {
        UserEntity userEntity = UserEntity.builder().build();
        when(userDAO.findByEmail(any())).thenReturn(userEntity);

        Date expiresAt = new Date();
        SessionToken sessionToken = SessionToken.builder()
                .userIdentifier(new UserIdentifier("soUnique"))
                .token("soRandomToken")
                .expiresAt(expiresAt)
                .build();

        sessionTokenRepository.add(sessionToken);

        verify(sessionTokenDAO).save(sessionTokenEntityArgumentCaptor.capture());
        SessionTokenEntity sessionTokenEntity = sessionTokenEntityArgumentCaptor.getValue();
        assertThat(sessionTokenEntity.userEntity).isEqualTo(userEntity);
        assertThat(sessionTokenEntity.token).isEqualTo("soRandomToken");
        assertThat(sessionTokenEntity.expiresAt).isEqualTo(expiresAt);
        assertThat(sessionTokenEntity.createdAt).isToday();
        assertThat(sessionTokenEntity.updatedAt).isToday();
    }

    @Test
    public void add_sessionToken_findsUser_whenNotFound_throwsAbnormalModelException() throws Exception {
        when(userDAO.findByEmail(any())).thenReturn(null);

        Date expiresAt = new Date();
        SessionToken sessionToken = SessionToken.builder()
                .userIdentifier(new UserIdentifier("soUnique"))
                .token("soRandomToken")
                .expiresAt(expiresAt)
                .build();

        exception.expect(AbnormalModelException.class);
        sessionTokenRepository.add(sessionToken);
    }

    @Test
    public void find_callsSessionTokenDao_whenSessionTokenFound_withUser_returnsOptionalWithSessionToken() throws Exception {
        SessionTokenEntity sessionTokenEntity = SessionTokenEntity.builder()
                .token("bananas")
                .expiresAt(new Date())
                .userEntity(UserEntity.builder().email("chimi@chonga.com").build())
                .build();
        when(sessionTokenDAO.findByToken(any())).thenReturn(sessionTokenEntity);

        Optional<SessionToken> sessionTokenOptional = sessionTokenRepository.find(new SessionTokenIdentifier("suchSecretToken"));

        verify(sessionTokenDAO).findByToken("suchSecretToken");
        assertThat(sessionTokenOptional.isPresent()).isTrue();
        SessionToken sessionToken = sessionTokenOptional.get();
        assertThat(sessionToken.getToken()).isEqualTo("bananas");
        assertThat(sessionToken.getExpiresAt()).isToday();
        assertThat(sessionToken.getUserIdentifier()).isEqualTo(new UserIdentifier("chimi@chonga.com"));
    }

    @Test
    public void find_callsSessionTokenDao_whenSessionTokenFound_withoutUser_returnsEmptyOptional() throws Exception {
        SessionTokenEntity sessionTokenEntity = SessionTokenEntity.builder()
                .token("bananas")
                .expiresAt(new Date())
                .userEntity(null)
                .build();
        when(sessionTokenDAO.findByToken(any())).thenReturn(sessionTokenEntity);

        Optional<SessionToken> sessionTokenOptional = sessionTokenRepository.find(new SessionTokenIdentifier("suchSecretToken"));

        verify(sessionTokenDAO).findByToken("suchSecretToken");
        assertThat(sessionTokenOptional.isPresent()).isFalse();
    }

    @Test
    public void find_callsSessionTokenDao_whenSessionTokenNotFound_returnsEmptyOptional() throws Exception {
        when(sessionTokenDAO.findByToken(any())).thenReturn(null);

        Optional<SessionToken> sessionTokenOptional = sessionTokenRepository.find(new SessionTokenIdentifier("suchSecretToken"));

        verify(sessionTokenDAO).findByToken("suchSecretToken");
        assertThat(sessionTokenOptional.isPresent()).isFalse();
    }
}