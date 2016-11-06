package com.doerapispring;

import com.doerapispring.users.UserDAO;
import com.doerapispring.users.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

/**
 * Created by chiragtailor on 11/5/16.
 */
@Repository
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
}
