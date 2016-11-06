package com.doerapispring.userSessions;

import com.doerapispring.Credentials;
import com.doerapispring.UserIdentifier;
import com.doerapispring.apiTokens.SessionToken;
import com.doerapispring.apiTokens.SessionTokenService;
import com.doerapispring.users.NewUser;
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

    public SessionToken signup(UserIdentifier userIdentifier, Credentials credentials) {
        NewUser user = userService.create(userIdentifier);
        authenticationService.registerCredentials(userIdentifier, credentials);
        if (user == null) return null;
        return sessionTokenService.grant(userIdentifier);
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
}
