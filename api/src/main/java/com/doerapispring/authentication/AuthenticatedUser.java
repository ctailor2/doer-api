package com.doerapispring.authentication;

import com.doerapispring.domain.ListId;
import com.doerapispring.domain.User;
import com.doerapispring.domain.UserId;

import java.util.Objects;

public class AuthenticatedUser {
    private final String identifier;
    private final String listId;

    public AuthenticatedUser(String identifier, String listId) {
        this.identifier = identifier;
        this.listId = listId;
    }

    public String getIdentifier() {
        return identifier;
    }

    public User getUser() {
        return new User(new UserId(identifier), new ListId(listId));
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AuthenticatedUser that = (AuthenticatedUser) o;
        return Objects.equals(identifier, that.identifier) &&
            Objects.equals(listId, that.listId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(identifier, listId);
    }

    @Override
    public String toString() {
        return "AuthenticatedUser{" +
            "identifier='" + identifier + '\'' +
            ", listId='" + listId + '\'' +
            '}';
    }
}
