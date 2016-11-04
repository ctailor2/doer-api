package com.doerapispring.users;

import com.doerapispring.Identifier;

/**
 * Created by chiragtailor on 11/3/16.
 */
public class NewUser {
    private final Identifier identifier;

    public NewUser(Identifier identifier) {
        this.identifier = identifier;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NewUser newUser = (NewUser) o;

        return identifier != null ? identifier.equals(newUser.identifier) : newUser.identifier == null;

    }

    @Override
    public int hashCode() {
        return identifier != null ? identifier.hashCode() : 0;
    }
}
