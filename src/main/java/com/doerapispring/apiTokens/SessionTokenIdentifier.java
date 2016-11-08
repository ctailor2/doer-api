package com.doerapispring.apiTokens;

/**
 * Created by chiragtailor on 11/7/16.
 */
public class SessionTokenIdentifier {
    private final String identifier;

    public SessionTokenIdentifier(String identifier) {
        this.identifier = identifier;
    }

    public String get() {
        return identifier;
    }
}
