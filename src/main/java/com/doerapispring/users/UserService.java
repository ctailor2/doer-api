package com.doerapispring.users;

import com.doerapispring.userSessions.UserSessionsService;
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

    public UserEntity create(UserEntity userEntity) {
        User user = User.builder()
                .email(userEntity.getEmail())
                .passwordDigest(passwordEncoder.encode(userEntity.getPassword()))
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        userRepository.save(user);
        UserEntity savedUserEntity = UserEntity.builder()
                .email(userEntity.getEmail())
                .build();
        return savedUserEntity;
    }
}
