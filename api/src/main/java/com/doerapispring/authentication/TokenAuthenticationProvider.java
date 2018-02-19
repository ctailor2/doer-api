package com.doerapispring.authentication;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class TokenAuthenticationProvider implements AuthenticationProvider {

    private final AuthenticationTokenService authenticationTokenService;

    @Autowired
    public TokenAuthenticationProvider(AuthenticationTokenService authenticationTokenService) {
        this.authenticationTokenService = authenticationTokenService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        PreAuthenticatedAuthenticationToken preAuthenticatedAuthenticationToken = (PreAuthenticatedAuthenticationToken) authentication;
        String accessToken = preAuthenticatedAuthenticationToken.getCredentials();
        Optional<TransientAccessToken> tokenOptional = authenticationTokenService.retrieve(accessToken);
        if (tokenOptional.isPresent()) {
            AuthenticatedUser authenticatedUser = AuthenticatedUser.identifiedWith(tokenOptional.get().getAuthenticatedEntityIdentifier());
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
