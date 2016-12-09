package com.doerapispring;

import com.doerapispring.users.UserDAO;
import com.doerapispring.users.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
public class UserCredentialsRepository {
    private final UserDAO userDAO;

    @Autowired
    public UserCredentialsRepository(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void add(UserCredentials userCredentials) {
        UserEntity userEntity = userDAO.findByEmail(userCredentials.getUserIdentifier().get());
        userEntity.passwordDigest = userCredentials.getEncodedCredentials().get();
        userDAO.save(userEntity);
    }

    public Optional<UserCredentials> find(UserIdentifier userIdentifier) {
        UserEntity userEntity = userDAO.findByEmail(userIdentifier.get());
        if (userEntity == null) return Optional.empty();
        return Optional.of(new UserCredentials(userIdentifier,
                new EncodedCredentials(userEntity.passwordDigest)));
    }
}
