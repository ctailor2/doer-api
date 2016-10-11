package com.doerapispring.users;

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
public class UserService {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
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
}
