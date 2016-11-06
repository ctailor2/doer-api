package com.doerapispring;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SignupForm {
    private final UserIdentifier userIdentifier;
    private final Credentials credentials;

    @JsonCreator
    public SignupForm(@JsonProperty("identifier") UserIdentifier userIdentifier,
                      @JsonProperty("credentials") Credentials credentials) {
        this.userIdentifier = userIdentifier;
        this.credentials = credentials;
    }

    public UserIdentifier getIdentifier() {
        return userIdentifier;
    }

    public Credentials getCredentials() {
        return credentials;
    }
}
