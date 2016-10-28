package com.doerapispring.userSessions;

import com.doerapispring.apiTokens.SessionToken;
import com.doerapispring.apiTokens.SessionTokenService;
import com.doerapispring.apiTokens.UserSession;
import com.doerapispring.users.RegisteredUser;
import com.doerapispring.users.User;
import com.doerapispring.users.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * Created by chiragtailor on 9/1/16.
 */
@Service
public class UserSessionsService {
    private final UserService userService;
    private final SessionTokenService sessionTokenService;
    private final AuthenticationService authenticationService;

    @Autowired
    public UserSessionsService(UserService userService, SessionTokenService sessionTokenService, AuthenticationService authenticationService) {
        this.userService = userService;
        this.sessionTokenService = sessionTokenService;
        this.authenticationService = authenticationService;
    }

    public User signup(User user) {
        User savedUser = userService.create(user);
        SessionToken sessionToken = sessionTokenService.create(savedUser.getEmail());
        savedUser.setSessionToken(sessionToken);
        return savedUser;
    }

    public User login(User user) {
        boolean authResult = authenticationService.authenticate(user.getEmail(), user.getPassword());
        if (!authResult) return null;
        SessionToken sessionToken = sessionTokenService.getActive(user.getEmail());
        if (sessionToken == null) sessionToken = sessionTokenService.create(user.getEmail());
        user.setSessionToken(sessionToken);
        return user;
    }

    public void logout(String userEmail) {
        sessionTokenService.expire(userEmail);
    }

    public User newSignup(String email, String password) {
        RegisteredUser registeredUser = userService.createRegisteredUser(email, password);
        UserSession userSession = sessionTokenService.start(registeredUser);
        return User.builder()
                .email(userSession.getEmail())
                .sessionToken(SessionToken.builder()
                    .token(userSession.getToken())
                    .expiresAt(userSession.getExpiresAt())
                    .build())
                .build();
    }
}
