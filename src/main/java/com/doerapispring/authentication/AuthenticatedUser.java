package com.doerapispring.authentication;

import com.doerapispring.domain.UniqueIdentifier;
import com.doerapispring.domain.User;

public class AuthenticatedUser {
    private final String identifier;

    public static AuthenticatedUser identifiedWith(String identifier) {
        return new AuthenticatedUser(identifier);
    }

    public AuthenticatedUser(String identifier) {
        this.identifier = identifier;
    }

    public String getIdentifier() {
        return identifier;
    }

    public User getUser() {
        return new User(new UniqueIdentifier(identifier));
    }
}
