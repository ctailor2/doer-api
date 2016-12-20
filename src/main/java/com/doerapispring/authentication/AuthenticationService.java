package com.doerapispring.authentication;

import com.doerapispring.domain.AbnormalModelException;
import com.doerapispring.domain.DomainRepository;
import com.doerapispring.domain.UserIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthenticationService {
    private PasswordEncoder passwordEncoder;
    private final DomainRepository<UserCredentials, String> userCredentialsRepository;

    @Autowired
    public AuthenticationService(PasswordEncoder passwordEncoder,
                                 DomainRepository<UserCredentials, String> userCredentialsRepository) {
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
