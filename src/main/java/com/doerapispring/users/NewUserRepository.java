package com.doerapispring.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * Created by chiragtailor on 10/24/16.
 */
@Repository
public class NewUserRepository {
    private final UserDAO userDAO;

    @Autowired
    public NewUserRepository(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public void add(RegisteredUser registeredUser) {
        UserEntity userEntity = UserEntity.builder()
                .email(registeredUser.getEmail())
                .passwordDigest(registeredUser.getEncodedPassword())
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        userDAO.save(userEntity);
    }
}
