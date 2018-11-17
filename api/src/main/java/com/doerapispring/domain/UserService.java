package com.doerapispring.domain;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final ObjectRepository<User, UserId> userRepository;

    UserService(ObjectRepository<User, UserId> userRepository) {
        this.userRepository = userRepository;
    }

    public User create(String identifier) throws OperationRefusedException {
        UserId userId = new UserId(identifier);
        Optional<User> userOptional = userRepository.find(userId);
        if (userOptional.isPresent()) throw new OperationRefusedException();
        User user = new User(userId);
        userRepository.add(user);
        return user;
    }
}
