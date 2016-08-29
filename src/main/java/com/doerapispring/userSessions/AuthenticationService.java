package com.doerapispring.userSessions;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Created by chiragtailor on 8/29/16.
 */
@Service
class AuthenticationService {
    private PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }

    boolean authenticate(String password, String passwordDigest) {
        return passwordEncoder.matches(password, passwordDigest);
    }
}
