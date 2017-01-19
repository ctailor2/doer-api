package com.doerapispring.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private ObjectRepository<User, String> userRepository;

    @Autowired
    public UserService(ObjectRepository<User, String> userRepository) {
        this.userRepository = userRepository;
    }

    public User create(String identifier) throws OperationRefusedException {
        UniqueIdentifier uniqueIdentifier = new UniqueIdentifier<>(identifier);
        Optional<User> userOptional = userRepository.find(uniqueIdentifier);
        if (userOptional.isPresent()) throw new OperationRefusedException();
        User user = new User(uniqueIdentifier);
        try {
            userRepository.add(user);
        } catch (AbnormalModelException e) {
            throw new OperationRefusedException();
        }
        return user;
    }
}
