package com.doerapispring.domain;

import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final ObjectRepository<User, String> userRepository;

    UserService(ObjectRepository<User, String> userRepository) {
        this.userRepository = userRepository;
    }

    public User create(String identifier) throws OperationRefusedException {
        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>(identifier);
        Optional<User> userOptional = userRepository.find(uniqueIdentifier);
        if (userOptional.isPresent()) throw new OperationRefusedException();
        User user = new User(uniqueIdentifier);
        userRepository.add(user);
        return user;
    }
}
