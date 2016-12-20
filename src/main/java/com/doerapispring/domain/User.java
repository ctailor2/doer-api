package com.doerapispring.domain;

public class User implements UniquelyIdentifiable {
    private final UserIdentifier userIdentifier;

    public User(UserIdentifier userIdentifier) {
        this.userIdentifier = userIdentifier;
    }

    @Override
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
