package com.doerapispring.storage;

import com.doerapispring.authentication.EncodedCredentials;
import com.doerapispring.authentication.UserCredentials;
import com.doerapispring.domain.ObjectRepository;
import com.doerapispring.domain.UniqueIdentifier;
import com.doerapispring.domain.UserIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Repository
@Transactional
class UserCredentialsRepository implements ObjectRepository<UserCredentials, String> {
    private final UserDAO userDAO;

    @Autowired
    UserCredentialsRepository(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public void add(UserCredentials userCredentials) {
        UserEntity userEntity = userDAO.findByEmail(userCredentials.getIdentifier().get());
        userEntity.passwordDigest = userCredentials.getEncodedCredentials().get();
        userDAO.save(userEntity);
    }

    @Override
    public Optional<UserCredentials> find(UniqueIdentifier<String> uniqueIdentifier) {
        String email = uniqueIdentifier.get();
        UserEntity userEntity = userDAO.findByEmail(email);
        if (userEntity == null) return Optional.empty();
        return Optional.of(new UserCredentials(new UserIdentifier(email),
                new EncodedCredentials(userEntity.passwordDigest)));
    }
}
