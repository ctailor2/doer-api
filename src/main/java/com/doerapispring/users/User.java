package com.doerapispring.users;

import com.doerapispring.UserIdentifier;

public class User {
    private final UserIdentifier userIdentifier;

    public User(UserIdentifier userIdentifier) {
        this.userIdentifier = userIdentifier;
    }

    public UserIdentifier getIdentifier() {
        return userIdentifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return userIdentifier != null ? userIdentifier.equals(user.userIdentifier) : user.userIdentifier == null;

    }

    @Override
    public int hashCode() {
        return userIdentifier != null ? userIdentifier.hashCode() : 0;
    }
}
