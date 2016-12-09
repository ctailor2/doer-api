package com.doerapispring.userSessions;

import com.doerapispring.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationService {
    private PasswordEncoder passwordEncoder;
    private final UserCredentialsRepository userCredentialsRepository;

    @Autowired
    public AuthenticationService(PasswordEncoder passwordEncoder,
                                 UserCredentialsRepository userCredentialsRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userCredentialsRepository = userCredentialsRepository;
    }

    public void registerCredentials(UserIdentifier userIdentifier, Credentials credentials) {
        EncodedCredentials encodedCredentials = new EncodedCredentials(passwordEncoder.encode(credentials.get()));
        UserCredentials userCredentials = new UserCredentials(userIdentifier, encodedCredentials);
        userCredentialsRepository.add(userCredentials);
    }

    public boolean authenticate(UserIdentifier userIdentifier, Credentials credentials) {
        Optional<UserCredentials> userCredentialsOptional = userCredentialsRepository.find(userIdentifier);
        if (!userCredentialsOptional.isPresent()) return false;
        return passwordEncoder.matches(credentials.get(),
                userCredentialsOptional.get().getEncodedCredentials().get());
    }
}
