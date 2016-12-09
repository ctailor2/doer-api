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
public class UserService {
    private UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User create(UserIdentifier userIdentifier) throws OperationRefusedException {
        Optional<User> userOptional = userRepository.find(userIdentifier);
        if (userOptional.isPresent()) throw new OperationRefusedException();
        User user = new User(userIdentifier);
        userRepository.add(user);
        return user;
    }
}
