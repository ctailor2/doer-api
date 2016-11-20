package com.doerapispring.config;

import com.doerapispring.apiTokens.AuthenticationToken;
import com.doerapispring.apiTokens.SessionToken;
import com.doerapispring.apiTokens.SessionTokenIdentifier;
import com.doerapispring.apiTokens.SessionTokenService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Created by chiragtailor on 9/19/16.
 */
@Component
public class TokenAuthenticationProvider implements AuthenticationProvider {

    private final SessionTokenService sessionTokenService;

    @Autowired
    public TokenAuthenticationProvider(SessionTokenService sessionTokenService) {
        this.sessionTokenService = sessionTokenService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        AuthenticationToken authenticationToken = (AuthenticationToken) authentication;
        SessionTokenIdentifier sessionTokenIdentifier = new SessionTokenIdentifier(authenticationToken.getCredentials());
        Optional<SessionToken> sessionToken = sessionTokenService.getByToken(sessionTokenIdentifier);
        if (sessionToken.isPresent()) {
            authenticationToken.setAuthenticated(true);
            authenticationToken.setPrincipal(sessionToken.get().getUserIdentifier());
            return authenticationToken;
        } else {
            throw new AuthenticationCredentialsNotFoundException("Authentication required");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (AuthenticationToken.class.isAssignableFrom(authentication));
    }
}
