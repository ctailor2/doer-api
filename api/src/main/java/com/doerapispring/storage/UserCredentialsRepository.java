package com.doerapispring.storage;

import com.doerapispring.authentication.Credentials;
import com.doerapispring.authentication.CredentialsStore;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Repository
@Transactional
class UserCredentialsRepository implements CredentialsStore {
    private final UserDAO userDAO;

    UserCredentialsRepository(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public void add(Credentials credentials) {
        UserEntity userEntity = userDAO.findByEmail(credentials.getUserIdentifier());
        userEntity.passwordDigest = credentials.getSecret();
        userDAO.save(userEntity);
    }

    @Override
    public Optional<Credentials> findLatest(String userIdentifier) {
        UserEntity userEntity = userDAO.findByEmail(userIdentifier);
        if (userEntity == null) return Optional.empty();
        return Optional.of(new Credentials(userIdentifier,
                userEntity.passwordDigest,
                new Date()));
    }
}
