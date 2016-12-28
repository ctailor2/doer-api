package com.doerapispring.storage;

import com.doerapispring.domain.ObjectRepository;
import com.doerapispring.domain.UniqueIdentifier;
import com.doerapispring.domain.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Repository
@Transactional
class UserRepository implements ObjectRepository<User, String> {
    private final UserDAO userDAO;

    @Autowired
    UserRepository(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public void add(User user) {
        UserEntity userEntity = UserEntity.builder()
                .email(user.getIdentifier().get())
                .passwordDigest("")
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        userDAO.save(userEntity);
    }

    @Override
    public Optional<User> find(UniqueIdentifier<String> uniqueIdentifier) {
        String email = uniqueIdentifier.get();
        UserEntity userEntity = userDAO.findByEmail(email);
        if (userEntity == null) return Optional.empty();
        return Optional.of(new User(new UniqueIdentifier(email)));
    }
}
