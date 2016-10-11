package com.doerapispring.userSessions;

import com.doerapispring.users.UserEntity;
import com.doerapispring.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Created by chiragtailor on 8/29/16.
 */
@Service
public class AuthenticationService {
    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationService(PasswordEncoder passwordEncoder, UserRepository userRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    public boolean authenticate(String email, String password) {
        UserEntity userEntity = userRepository.findByEmail(email);
        if (userEntity == null) return false;
        return passwordEncoder.matches(password, userEntity.passwordDigest);
    }
}
