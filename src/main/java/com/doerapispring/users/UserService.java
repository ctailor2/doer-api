package com.doerapispring.users;

import com.doerapispring.utilities.PasswordEncodingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Created by chiragtailor on 8/12/16.
 */
@Service
@Transactional
public class UserService implements UserServiceInterface {
    private UserRepository userRepository;
    private NewUserRepository newUserRepository;
    private PasswordEncodingService passwordEncodingService;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository,
                       NewUserRepository newUserRepository,
                       PasswordEncodingService passwordEncodingService,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.newUserRepository = newUserRepository;
        this.passwordEncodingService = passwordEncodingService;
        this.passwordEncoder = passwordEncoder;
    }

    public User create(User user) {
        UserEntity userEntity = UserEntity.builder()
                .email(user.getEmail())
                .passwordDigest(passwordEncoder.encode(user.getPassword()))
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        userRepository.save(userEntity);
        User savedUser = User.builder()
                .email(user.getEmail())
                .build();
        return savedUser;
    }

    public RegisteredUser createRegisteredUser(String email, String password) {
        String encodedPassword = passwordEncodingService.encode(password);
        RegisteredUser registeredUser = new RegisteredUser(email, encodedPassword);
        newUserRepository.add(registeredUser);
        return registeredUser;
    }
}
