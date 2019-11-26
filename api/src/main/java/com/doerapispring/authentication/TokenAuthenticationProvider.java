package com.doerapispring.authentication;

import com.doerapispring.domain.UserService;
import org.springframework.security.authentication.AccountExpiredException;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
class TokenAuthenticationProvider implements AuthenticationProvider {

    private final AuthenticationTokenService authenticationTokenService;
    private final UserService userService;

    public TokenAuthenticationProvider(AuthenticationTokenService authenticationTokenService,
                                       UserService userService) {
        this.authenticationTokenService = authenticationTokenService;
        this.userService = userService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        PreAuthenticatedAuthenticationToken preAuthenticatedAuthenticationToken = (PreAuthenticatedAuthenticationToken) authentication;
        String accessToken = preAuthenticatedAuthenticationToken.getCredentials();
        Optional<TransientAccessToken> tokenOptional = authenticationTokenService.retrieve(accessToken);
        if (tokenOptional.isPresent()) {
            Optional<AuthenticatedAuthenticationToken> authenticatedAuthenticationToken = userService.find(tokenOptional.get().getAuthenticatedEntityIdentifier())
                .map(user -> new AuthenticatedUser(user.getUserId().get(), user.getDefaultListId().get()))
                .map(AuthenticatedAuthenticationToken::new);
            if (authenticatedAuthenticationToken.isPresent()) {
                return authenticatedAuthenticationToken.get();
            } else {
                throw new AccountExpiredException("Account has expired.");
            }
        } else {
            throw new AuthenticationCredentialsNotFoundException("Authentication required");
        }
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return (PreAuthenticatedAuthenticationToken.class.isAssignableFrom(authentication));
    }
}
