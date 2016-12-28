package com.doerapispring.authentication;

import com.doerapispring.domain.AbnormalModelException;
import com.doerapispring.domain.OperationRefusedException;
import com.doerapispring.domain.UniqueIdentifier;
import com.doerapispring.domain.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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

    public SessionToken signup(UniqueIdentifier uniqueIdentifier, Credentials credentials) throws OperationRefusedException, AbnormalModelException {
        userService.create(uniqueIdentifier);
        authenticationService.registerCredentials(uniqueIdentifier, credentials);
        return sessionTokenService.grant(uniqueIdentifier);
    }

    public SessionToken login(UniqueIdentifier uniqueIdentifier, Credentials credentials) throws AccessDeniedException {
        boolean authenticationResult = authenticationService.authenticate(uniqueIdentifier, credentials);
        if (!authenticationResult) throw new AccessDeniedException();
        try {
            return sessionTokenService.grant(uniqueIdentifier);
        } catch (OperationRefusedException e) {
            throw new AccessDeniedException();
        }
    }
}
