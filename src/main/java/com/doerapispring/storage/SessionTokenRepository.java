package com.doerapispring.storage;

import com.doerapispring.authentication.SessionToken;
import com.doerapispring.domain.AbnormalModelException;
import com.doerapispring.domain.ObjectRepository;
import com.doerapispring.domain.UniqueIdentifier;
import com.doerapispring.domain.UserIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

@Repository
class SessionTokenRepository implements ObjectRepository<SessionToken, String> {
    private final UserDAO userDAO;
    private final SessionTokenDAO sessionTokenDAO;

    @Autowired
    SessionTokenRepository(UserDAO userDAO, SessionTokenDAO sessionTokenDAO) {
        this.userDAO = userDAO;
        this.sessionTokenDAO = sessionTokenDAO;
    }

    @Override
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

    @Override
    public Optional<SessionToken> find(UniqueIdentifier<String> uniqueIdentifier) {
        String token = uniqueIdentifier.get();
        SessionTokenEntity sessionTokenEntity = sessionTokenDAO.findByToken(token);
        if (sessionTokenEntity == null || sessionTokenEntity.userEntity == null) return Optional.empty();
        return Optional.of(
                SessionToken.builder()
                        .token(sessionTokenEntity.token)
                        .expiresAt(sessionTokenEntity.expiresAt)
                        .userIdentifier(new UserIdentifier(sessionTokenEntity.userEntity.email))
                        .build());
    }
}
