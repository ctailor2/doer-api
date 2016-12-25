package com.doerapispring.authentication;

import com.doerapispring.domain.AbnormalModelException;
import com.doerapispring.domain.ObjectRepository;
import com.doerapispring.domain.UserIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
class AuthenticationService {
    private PasswordEncoder passwordEncoder;
    private final ObjectRepository<UserCredentials, String> userCredentialsRepository;

    @Autowired
    public AuthenticationService(PasswordEncoder passwordEncoder,
                                 ObjectRepository<UserCredentials, String> userCredentialsRepository) {
        this.passwordEncoder = passwordEncoder;
        this.userCredentialsRepository = userCredentialsRepository;
    }

    public void registerCredentials(UserIdentifier userIdentifier, Credentials credentials) throws AbnormalModelException {
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
