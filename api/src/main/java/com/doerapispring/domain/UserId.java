package com.doerapispring.domain;

public class UserId {
    private final String identifier;

    public UserId(String identifier) {
        this.identifier = identifier;
    }

    public String get() {
        return identifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        UserId userId = (UserId) o;

        return identifier != null ? identifier.equals(userId.identifier) : userId.identifier == null;
    }

    @Override
    public int hashCode() {
        return identifier != null ? identifier.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "UserId{" +
            "identifier='" + identifier + '\'' +
            '}';
    }
}
