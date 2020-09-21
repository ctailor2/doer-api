package com.doerapispring.storage;

import com.doerapispring.authentication.TransientAccessToken;
import com.doerapispring.session.InvalidTokenException;
import com.doerapispring.session.SessionToken;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class SessionTokenRepositoryTest {
    private SessionTokenRepository sessionTokenRepository;

    private UserDAO userDAO;

    private SessionTokenDAO sessionTokenDAO;

    private final ArgumentCaptor<SessionTokenEntity> sessionTokenEntityArgumentCaptor = ArgumentCaptor.forClass(SessionTokenEntity.class);

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        userDAO = mock(UserDAO.class);
        sessionTokenDAO = mock(SessionTokenDAO.class);
        sessionTokenRepository = new SessionTokenRepository(userDAO, sessionTokenDAO);
    }

    @Test
    public void add_sessionToken_findsUser_whenFound_savesRelationship_savesFields_setsAuditingData() throws Exception {
        UserEntity userEntity = new UserEntity();
        when(userDAO.findByEmail(any())).thenReturn(userEntity);

        Date expiresAt = new Date();
        SessionToken sessionToken = new SessionToken("soUnique", "soRandomToken", expiresAt);

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
        SessionToken sessionToken = new SessionToken("soUnique", "soRandomToken", expiresAt);

        exception.expect(InvalidTokenException.class);
        sessionTokenRepository.add(sessionToken);
    }

    @Test
    public void find_callsSessionTokenDao_whenSessionTokenFound_withUser_returnsOptionalWithSessionToken() {
        UserEntity userEntity = new UserEntity();
        userEntity.setEmail("chimi@chonga.com");
        SessionTokenEntity sessionTokenEntity = new SessionTokenEntity();
        sessionTokenEntity.setToken("bananas");
        sessionTokenEntity.setExpiresAt(new Date());
        sessionTokenEntity.setUserEntity(userEntity);
        when(sessionTokenDAO.findByToken(any())).thenReturn(sessionTokenEntity);

        Optional<TransientAccessToken> tokenOptional = sessionTokenRepository.find("suchSecretToken");

        verify(sessionTokenDAO).findByToken("suchSecretToken");
        assertThat(tokenOptional.isPresent()).isTrue();
        TransientAccessToken token = tokenOptional.get();
        assertThat(token.getAccessToken()).isEqualTo("bananas");
        assertThat(token.getExpiresAt()).isToday();
        assertThat(token.getAuthenticatedEntityIdentifier()).isEqualTo("chimi@chonga.com");
    }

    @Test
    public void find_callsSessionTokenDao_whenSessionTokenFound_withoutUser_returnsEmptyOptional() {
        SessionTokenEntity sessionTokenEntity = new SessionTokenEntity();
        sessionTokenEntity.setToken("bananas");
        sessionTokenEntity.setExpiresAt(new Date());
        sessionTokenEntity.setUserEntity(null);
        when(sessionTokenDAO.findByToken(any())).thenReturn(sessionTokenEntity);

        Optional<TransientAccessToken> tokenOptional = sessionTokenRepository.find("suchSecretToken");

        verify(sessionTokenDAO).findByToken("suchSecretToken");
        assertThat(tokenOptional.isPresent()).isFalse();
    }

    @Test
    public void find_callsSessionTokenDao_whenSessionTokenNotFound_returnsEmptyOptional() {
        when(sessionTokenDAO.findByToken(any())).thenReturn(null);

        Optional<TransientAccessToken> tokenOptional = sessionTokenRepository.find("suchSecretToken");

        verify(sessionTokenDAO).findByToken("suchSecretToken");
        assertThat(tokenOptional.isPresent()).isFalse();
    }
}