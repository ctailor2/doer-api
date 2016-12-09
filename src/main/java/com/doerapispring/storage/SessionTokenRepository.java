package com.doerapispring.storage;

import com.doerapispring.authentication.SessionToken;
import com.doerapispring.authentication.SessionTokenIdentifier;
import com.doerapispring.domain.AbnormalModelException;
import com.doerapispring.domain.UserIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
public class SessionTokenRepository {
    private final UserDAO userDAO;
    private final SessionTokenDAO sessionTokenDAO;

    @Autowired
    public SessionTokenRepository(UserDAO userDAO, SessionTokenDAO sessionTokenDAO) {
        this.userDAO = userDAO;
        this.sessionTokenDAO = sessionTokenDAO;
    }

    public void add(SessionToken sessionToken) throws AbnormalModelException {
        UserEntity userEntity = userDAO.findByEmail(sessionToken.getUserIdentifier().get());
        if (userEntity == null) throw new AbnormalModelException();
        SessionTokenEntity sessionTokenEntity = SessionTokenEntity.builder()
                .userEntity(userEntity)
                .token(sessionToken.getToken())
                .expiresAt(sessionToken.getExpiresAt())
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        sessionTokenDAO.save(sessionTokenEntity);
    }

    public Optional<SessionToken> find(SessionTokenIdentifier sessionTokenIdentifier) {
        SessionTokenEntity sessionTokenEntity = sessionTokenDAO.findByToken(sessionTokenIdentifier.get());
        if (sessionTokenEntity == null || sessionTokenEntity.userEntity == null) return Optional.empty();
        return Optional.of(
                SessionToken.builder()
                        .token(sessionTokenEntity.token)
                        .expiresAt(sessionTokenEntity.expiresAt)
                        .userIdentifier(new UserIdentifier(sessionTokenEntity.userEntity.email))
                        .build());
    }

    public void update(SessionToken sessionToken) {

    }
}
