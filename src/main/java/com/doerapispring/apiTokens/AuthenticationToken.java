package com.doerapispring.apiTokens;

import com.doerapispring.UserIdentifier;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

/**
 * Created by chiragtailor on 9/19/16.
 */
public class AuthenticationToken implements Authentication {
    private final String credentials;
    private boolean isAuthenticated = false;
    private UserIdentifier principal;

    public AuthenticationToken(String token) {
        credentials = token;
    }

    public void setPrincipal(UserIdentifier userIdentifier) {
        principal = userIdentifier;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return null;
    }

    @Override
    public String getCredentials() {
        return this.credentials;
    }

    @Override
    public Object getDetails() {
        return null;
    }

    @Override
    public Object getPrincipal() {
        return principal;
    }

    @Override
    public boolean isAuthenticated() {
        return this.isAuthenticated;
    }

    @Override
    public void setAuthenticated(boolean isAuthenticated) throws IllegalArgumentException {
    }

    @Override
    public String getName() {
        return null;
    }
}
