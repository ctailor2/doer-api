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
        return new User(new UniqueIdentifier<>(identifier));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AuthenticatedUser that = (AuthenticatedUser) o;

        return identifier != null ? identifier.equals(that.identifier) : that.identifier == null;

    }

    @Override
    public int hashCode() {
        return identifier != null ? identifier.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "AuthenticatedUser{" +
                "identifier='" + identifier + '\'' +
                '}';
    }
}
