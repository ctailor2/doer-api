package com.doerapispring.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.Optional;

@Service
class AuthenticationService implements BasicAuthenticationService {
    private PasswordEncoder passwordEncoder;
    private final CredentialsStore credentialsStore;

    @Autowired
    public AuthenticationService(PasswordEncoder passwordEncoder,
                                 CredentialsStore credentialsStore) {
        this.passwordEncoder = passwordEncoder;
        this.credentialsStore = credentialsStore;
    }

    public void registerCredentials(String userIdentifier, String credentials) {
        credentialsStore.add(new Credentials(userIdentifier,
                passwordEncoder.encode(credentials),
                new Date()));
    }

    public boolean authenticate(String userIdentifier, String secret) {
        Optional<Credentials> credentialsOptional = credentialsStore.findLatest(userIdentifier);
        if (!credentialsOptional.isPresent()) return false;
        Credentials credentials = credentialsOptional.get();
        return passwordEncoder.matches(secret, credentials.getSecret());
    }
}
