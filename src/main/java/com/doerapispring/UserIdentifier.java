package com.doerapispring;

/**
 * Created by chiragtailor on 11/3/16.
 */
public class UserIdentifier {
    private final String identifier;

    public UserIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String get() {
        return identifier;
    }
}
