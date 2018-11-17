package com.doerapispring.domain;

public class User implements UniquelyIdentifiable<String> {
    private UserId userId;

    public User(UserId userId) {
        this.userId = userId;
    }

    @Override
    public UniqueIdentifier<String> getIdentifier() {
        return new UniqueIdentifier<>(userId.get());
    }

    public UserId getUserId() {
        return userId;
    }
}
