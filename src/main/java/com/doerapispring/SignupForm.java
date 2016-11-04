package com.doerapispring;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SignupForm {
    private final Identifier identifier;
    private final Credentials credentials;

    @JsonCreator
    public SignupForm(@JsonProperty("identifier") Identifier identifier,
                      @JsonProperty("credentials") Credentials credentials) {
        this.identifier = identifier;
        this.credentials = credentials;
    }

    public Identifier getIdentifier() {
        return identifier;
    }

    public Credentials getCredentials() {
        return credentials;
    }
}
