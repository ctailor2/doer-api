package com.doerapispring.domain;

public class User implements UniquelyIdentifiable<String> {
    private final UniqueIdentifier<String> uniqueIdentifier;

    public User(UniqueIdentifier<String> uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    @Override
    public UniqueIdentifier<String> getIdentifier() {
        return uniqueIdentifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        User user = (User) o;

        return uniqueIdentifier != null ? uniqueIdentifier.equals(user.uniqueIdentifier) : user.uniqueIdentifier == null;

    }

    @Override
    public int hashCode() {
        return uniqueIdentifier != null ? uniqueIdentifier.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "User{" +
                "uniqueIdentifier=" + uniqueIdentifier +
                '}';
    }
}
