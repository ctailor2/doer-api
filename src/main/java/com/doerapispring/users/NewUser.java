package com.doerapispring.users;

import com.doerapispring.UserIdentifier;

/**
 * Created by chiragtailor on 11/3/16.
 */
public class NewUser {
    private final UserIdentifier userIdentifier;

    public NewUser(UserIdentifier userIdentifier) {
        this.userIdentifier = userIdentifier;
    }

    public UserIdentifier getIdentifier() {
        return userIdentifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        NewUser newUser = (NewUser) o;

        return userIdentifier != null ? userIdentifier.equals(newUser.userIdentifier) : newUser.userIdentifier == null;

    }

    @Override
    public int hashCode() {
        return userIdentifier != null ? userIdentifier.hashCode() : 0;
    }
}
