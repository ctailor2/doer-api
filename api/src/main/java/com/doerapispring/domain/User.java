package com.doerapispring.domain;

import java.util.Objects;

public class User {
    private UserId userId;
    private ListId defaultListId;

    public User(UserId userId, ListId defaultListId) {
        this.userId = userId;
        this.defaultListId = defaultListId;
    }

    public UserId getUserId() {
        return userId;
    }

    public ListId getDefaultListId() {
        return defaultListId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(userId, user.userId) &&
            Objects.equals(defaultListId, user.defaultListId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userId, defaultListId);
    }

    @Override
    public String toString() {
        return "User{" +
            "userId=" + userId +
            ", defaultListId=" + defaultListId +
            '}';
    }
}
