package com.doerapispring.userSessions;

import com.doerapispring.Credentials;
import com.doerapispring.UserIdentifier;
import com.doerapispring.apiTokens.SessionToken;
import com.doerapispring.apiTokens.SessionTokenService;
import com.doerapispring.users.NewUser;
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
        if (user == null) return null;
        authenticationService.registerCredentials(userIdentifier, credentials);
        return sessionTokenService.grant(userIdentifier);
    }

    public SessionToken login(UserIdentifier userIdentifier, Credentials credentials) {
        boolean authenticationResult = authenticationService.authenticate(userIdentifier, credentials);
        if (!authenticationResult) return null;
        return sessionTokenService.grant(userIdentifier);
    }
}
