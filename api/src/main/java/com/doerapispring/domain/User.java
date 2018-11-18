package com.doerapispring.domain;

public class User {
    private UserId userId;

    public User(UserId userId) {
        this.userId = userId;
    }

    public UserId getUserId() {
        return userId;
    }
}
