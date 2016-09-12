package com.doerapispring.userSessions;

import com.doerapispring.apiTokens.SessionToken;
import com.doerapispring.apiTokens.SessionTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Created by chiragtailor on 8/29/16.
 */
@Service
public class AuthenticationService {
    private SessionTokenService sessionTokenService;
    private PasswordEncoder passwordEncoder;

    @Autowired
    public AuthenticationService(PasswordEncoder passwordEncoder, SessionTokenService sessionTokenService) {
        this.passwordEncoder = passwordEncoder;
        this.sessionTokenService = sessionTokenService;
    }

    public boolean authenticatePassword(String password, String passwordDigest) {
        return passwordEncoder.matches(password, passwordDigest);
    }

    public boolean authenticateSessionToken(String token) {
        SessionToken sessionToken = sessionTokenService.getByToken(token);
        return sessionToken != null;
    }
}
