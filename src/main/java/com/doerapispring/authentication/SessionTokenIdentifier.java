package com.doerapispring.authentication;

import com.doerapispring.domain.UniqueIdentifier;

public class SessionTokenIdentifier implements UniqueIdentifier<String> {
    private final String identifier;

    public SessionTokenIdentifier(String identifier) {
        this.identifier = identifier;
    }

    @Override
    public String get() {
        return identifier;
    }
}
