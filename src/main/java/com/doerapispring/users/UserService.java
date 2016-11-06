package com.doerapispring.users;

import com.doerapispring.UserIdentifier;
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

    public NewUser create(UserIdentifier userIdentifier) {
        Optional<NewUser> userOptional = newUserRepository.find(userIdentifier);
        if (userOptional.isPresent()) return null;
        NewUser newUser = new NewUser(userIdentifier);
        newUserRepository.add(newUser);
        return newUser;
    }
}
