package com.doerapispring.apiTokens;

import com.doerapispring.users.UserDAO;
import com.doerapispring.users.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * Created by chiragtailor on 10/26/16.
 */
@Repository
public class NewSessionTokenRepository {
    private final UserDAO userDAO;
    private final SessionTokenDAO sessionTokenDAO;

    @Autowired
    public NewSessionTokenRepository(UserDAO userDAO, SessionTokenDAO sessionTokenDAO) {
        this.userDAO = userDAO;
        this.sessionTokenDAO = sessionTokenDAO;
    }

    public void add(UserSession userSession) {
        UserEntity userEntity = userDAO.findByEmail(userSession.getEmail());
        SessionTokenEntity sessionTokenEntity = SessionTokenEntity.builder()
                .userEntity(userEntity)
                .token(userSession.getToken())
                .expiresAt(userSession.getExpiresAt())
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        sessionTokenDAO.save(sessionTokenEntity);
    }

    public void add(SessionToken sessionToken) {
        UserEntity userEntity = userDAO.findByEmail(sessionToken.getIdentifier().get());
        SessionTokenEntity sessionTokenEntity = SessionTokenEntity.builder()
                .userEntity(userEntity)
                .token(sessionToken.getToken())
                .expiresAt(sessionToken.getExpiresAt())
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        sessionTokenDAO.save(sessionTokenEntity);
    }
}
