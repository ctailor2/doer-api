package com.doerapispring.authentication;

import com.doerapispring.domain.UniqueIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class TokenAuthenticationProvider implements AuthenticationProvider {

    private final SessionTokenService sessionTokenService;

    @Autowired
    public TokenAuthenticationProvider(SessionTokenService sessionTokenService) {
        this.sessionTokenService = sessionTokenService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        PreAuthenticatedAuthenticationToken preAuthenticatedAuthenticationToken = (PreAuthenticatedAuthenticationToken) authentication;
        UniqueIdentifier uniqueIdentifier = new UniqueIdentifier(preAuthenticatedAuthenticationToken.getCredentials());
        Optional<SessionToken> sessionToken = sessionTokenService.getByToken(uniqueIdentifier);
        if (sessionToken.isPresent()) {
            AuthenticatedUser authenticatedUser = AuthenticatedUser.identifiedWith(sessionToken.get().getUserIdentifier());
            return new AuthenticatedAuthenticationToken(authenticatedUser);
        } else {
            throw new AuthenticationCredentialsNotFoundException("Authentication required");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (PreAuthenticatedAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
