package com.doerapispring.domain;

import com.doerapispring.web.InvalidRequestException;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private final ObjectRepository<User, UserId> userRepository;

    UserService(ObjectRepository<User, UserId> userRepository) {
        this.userRepository = userRepository;
    }

    public User create(String identifier) throws InvalidRequestException {
        UserId userId = new UserId(identifier);
        Optional<User> userOptional = userRepository.find(userId);
        if (userOptional.isPresent()) throw new InvalidRequestException();
        User user = new User(userId);
        try {
            userRepository.save(user);
        } catch (AbnormalModelException e) {
            throw new InvalidRequestException();
        }
        return user;
    }
}
