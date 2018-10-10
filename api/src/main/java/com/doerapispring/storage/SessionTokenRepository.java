package com.doerapispring.storage;

import com.doerapispring.authentication.TransientAccessToken;
import com.doerapispring.session.InvalidTokenException;
import com.doerapispring.session.SessionToken;
import com.doerapispring.session.TokenStore;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
class SessionTokenRepository implements TokenStore {
    private final UserDAO userDAO;
    private final SessionTokenDAO sessionTokenDAO;

    SessionTokenRepository(UserDAO userDAO, SessionTokenDAO sessionTokenDAO) {
        this.userDAO = userDAO;
        this.sessionTokenDAO = sessionTokenDAO;
    }

    @Override
    public void add(TransientAccessToken token) throws InvalidTokenException {
        UserEntity userEntity = userDAO.findByEmail(token.getAuthenticatedEntityIdentifier());
        if (userEntity == null) throw new InvalidTokenException();
        SessionTokenEntity sessionTokenEntity = SessionTokenEntity.builder()
                .userEntity(userEntity)
                .token(token.getAccessToken())
                .expiresAt(token.getExpiresAt())
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        sessionTokenDAO.save(sessionTokenEntity);
    }

    @Override
    public Optional<TransientAccessToken> find(String accessToken) {
        SessionTokenEntity sessionTokenEntity = sessionTokenDAO.findByToken(accessToken);
        if (sessionTokenEntity == null || sessionTokenEntity.userEntity == null) return Optional.empty();
        return Optional.of(
                new SessionToken(sessionTokenEntity.userEntity.email,
                        sessionTokenEntity.token,
                        sessionTokenEntity.expiresAt));
    }
}
