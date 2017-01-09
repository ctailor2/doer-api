package com.doerapispring.web;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SignupForm {
    private final String identifier;
    private final String credentials;

    @JsonCreator
    public SignupForm(@JsonProperty("email") String identifier,
                      @JsonProperty("password") String credentials) {
        this.identifier = identifier;
        this.credentials = credentials;
    }

    public String getIdentifier() {
        return identifier;
    }

    public String getCredentials() {
        return credentials;
    }
}
