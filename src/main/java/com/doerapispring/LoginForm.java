package com.doerapispring;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by chiragtailor on 11/6/16.
 */
public class LoginForm {
    private final UserIdentifier userIdentifier;
    private final Credentials credentials;

    public LoginForm(@JsonProperty("identifier") UserIdentifier userIdentifier,
                     @JsonProperty("credentials") Credentials credentials) {
        this.userIdentifier = userIdentifier;
        this.credentials = credentials;
    }

    public UserIdentifier getUserIdentifier() {
        return userIdentifier;
    }

    public Credentials getCredentials() {
        return credentials;
    }
}