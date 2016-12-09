package com.doerapispring.authentication;

public class SessionTokenIdentifier {
    private final String identifier;

    public SessionTokenIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String get() {
        return identifier;
    }
}
