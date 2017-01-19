package com.doerapispring.authentication;

import com.doerapispring.domain.OperationRefusedException;
import com.doerapispring.domain.UserService;
import com.doerapispring.web.SessionTokenDTO;
import com.doerapispring.web.UserSessionsApiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserSessionsApiServiceImpl implements UserSessionsApiService {
    private final UserService userService;
    private final AuthenticationTokenService authenticationTokenService;
    private final BasicAuthenticationService authenticationService;

    @Autowired
    public UserSessionsApiServiceImpl(UserService userService,
                                      AuthenticationTokenService authenticationTokenService,
                                      BasicAuthenticationService authenticationService) {
        this.userService = userService;
        this.authenticationTokenService = authenticationTokenService;
        this.authenticationService = authenticationService;
    }

    @Override
    public SessionTokenDTO signup(String identifier, String credentials) throws AccessDeniedException {
        TransientAccessToken transientAccessToken;
        try {
            userService.create(identifier);
            authenticationService.registerCredentials(identifier, credentials);
            transientAccessToken = authenticationTokenService.grant(identifier);
        } catch (OperationRefusedException | CredentialsInvalidException | TokenRefusedException e) {
            throw new AccessDeniedException();
        }
        return new SessionTokenDTO(transientAccessToken.getAccessToken(),
                transientAccessToken.getExpiresAt());
    }

    @Override
    public SessionTokenDTO login(String identifier, String credentials) throws AccessDeniedException {
        boolean authenticationResult = authenticationService.authenticate(identifier, credentials);
        if (!authenticationResult) throw new AccessDeniedException();
        TransientAccessToken transientAccessToken;
        try {
            transientAccessToken = authenticationTokenService.grant(identifier);
        } catch (TokenRefusedException e) {
            throw new AccessDeniedException();
        }
        return new SessionTokenDTO(transientAccessToken.getAccessToken(),
                transientAccessToken.getExpiresAt());
    }
}
