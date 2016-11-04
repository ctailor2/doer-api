package com.doerapispring.users;

import com.doerapispring.Identifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.Optional;

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

    public Optional<NewUser> find(Identifier identifier) {
        UserEntity userEntity = userDAO.findByEmail(identifier.get());
        if(userEntity == null) return Optional.empty();
        return Optional.of(new NewUser(identifier));
    }

    public void add(NewUser newUser) {
        UserEntity userEntity = UserEntity.builder()
                .email(newUser.getIdentifier().get())
                .passwordDigest("")
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        userDAO.save(userEntity);
    }
}
