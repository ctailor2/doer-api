package com.doerapispring.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    private DomainRepository<User, String> userRepository;

    @Autowired
    public UserService(DomainRepository<User, String> userRepository) {
        this.userRepository = userRepository;
    }

    public User create(UserIdentifier userIdentifier) throws OperationRefusedException, AbnormalModelException {
        Optional<User> userOptional = userRepository.find(userIdentifier);
        if (userOptional.isPresent()) throw new OperationRefusedException();
        User user = new User(userIdentifier);
        userRepository.add(user);
        return user;
    }
}
