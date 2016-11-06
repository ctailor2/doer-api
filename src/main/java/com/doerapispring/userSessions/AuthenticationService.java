package com.doerapispring.userSessions;

import com.doerapispring.*;
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
    private final UserCredentialsRepository userCredentialsRepository;

    @Autowired
    public AuthenticationService(PasswordEncoder passwordEncoder,
                                 UserRepository userRepository,
                                 UserCredentialsRepository userCredentialsRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
        this.userCredentialsRepository = userCredentialsRepository;
    }

    public boolean authenticate(String email, String password) {
        UserEntity userEntity = userRepository.findByEmail(email);
        if (userEntity == null) return false;
        return passwordEncoder.matches(password, userEntity.passwordDigest);
    }

    public void registerCredentials(UserIdentifier userIdentifier, Credentials credentials) {
        EncodedCredentials encodedCredentials = new EncodedCredentials(passwordEncoder.encode(credentials.get()));
        UserCredentials userCredentials = new UserCredentials(userIdentifier, encodedCredentials);
        userCredentialsRepository.add(userCredentials);
    }
}
