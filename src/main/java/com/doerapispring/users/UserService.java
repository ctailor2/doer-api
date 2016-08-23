package com.doerapispring.users;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * Created by chiragtailor on 8/12/16.
 */
@Service
public class UserService {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void create(UserEntity userEntity) {
        User user = User.builder()
                .email(userEntity.getEmail())
                .password_digest(passwordEncoder.encode(userEntity.getPassword()))
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        System.out.println(userRepository.save(user));
    }
}
