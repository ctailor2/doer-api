package com.doerapispring.web;

import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginForm {
    private final String identifier;
    private final String credentials;

    LoginForm(@JsonProperty("email") String identifier,
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
