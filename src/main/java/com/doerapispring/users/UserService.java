package com.doerapispring.users;

import com.doerapispring.UserIdentifier;
import com.doerapispring.userSessions.OperationRefusedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * Created by chiragtailor on 8/12/16.
 */
@Service
public class UserService implements UserServiceInterface {
    private NewUserRepository newUserRepository;

    @Autowired
    public UserService(NewUserRepository newUserRepository) {
        this.newUserRepository = newUserRepository;
    }

    public User create(UserIdentifier userIdentifier) throws OperationRefusedException {
        Optional<User> userOptional = newUserRepository.find(userIdentifier);
        if (userOptional.isPresent()) throw new OperationRefusedException();
        User user = new User(userIdentifier);
        newUserRepository.add(user);
        return user;
    }
}
