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

    public UserId getId() {
        return new UserId(uniqueIdentifier.get());
    }
}
