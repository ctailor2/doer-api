package com.doerapispring.storage;

import com.doerapispring.domain.ListId;
import com.doerapispring.domain.ObjectRepository;
import com.doerapispring.domain.User;
import com.doerapispring.domain.UserId;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.Optional;

@Repository
@Transactional
class UserRepository implements ObjectRepository<User, UserId> {
    private final UserDAO userDAO;

    UserRepository(UserDAO userDAO) {
        this.userDAO = userDAO;
    }

    @Override
    public void save(User user) {
        UserEntity userEntity = UserEntity.builder()
            .email(user.getUserId().get())
            .defaultListId(user.getDefaultListId().get())
            .passwordDigest("")
            .createdAt(new Date())
            .updatedAt(new Date())
            .build();
        userDAO.save(userEntity);
    }

    @Override
    public Optional<User> find(UserId userId) {
        UserEntity userEntity = userDAO.findByEmail(userId.get());
        if (userEntity == null) return Optional.empty();
        User user = new User(
            new UserId(userEntity.email),
            new ListId(userEntity.defaultListId));
        return Optional.of(user);
    }
}
