package com.doerapispring.users;

import com.doerapispring.UserIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

/**
 * Created by chiragtailor on 10/24/16.
 */
@Repository
@Transactional
public class NewUserRepository {
    private final UserDAO userDAO;

    @Autowired
    public NewUserRepository(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    public Optional<User> find(UserIdentifier userIdentifier) {
        UserEntity userEntity = userDAO.findByEmail(userIdentifier.get());
        if(userEntity == null) return Optional.empty();
        return Optional.of(new User(userIdentifier));
    }

    public void add(User user) {
        UserEntity userEntity = UserEntity.builder()
                .email(user.getIdentifier().get())
                .passwordDigest("")
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        userDAO.save(userEntity);
    }
}
