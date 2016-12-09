package com.doerapispring.web;

import com.doerapispring.authentication.Credentials;
import com.doerapispring.domain.UserIdentifier;
import com.fasterxml.jackson.annotation.JsonProperty;

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
