package com.doerapispring.web;

import com.doerapispring.authentication.Credentials;
import com.doerapispring.domain.UniqueIdentifier;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class SignupForm {
    private final UniqueIdentifier uniqueIdentifier;
    private final Credentials credentials;

    @JsonCreator
    public SignupForm(@JsonProperty("identifier") UniqueIdentifier uniqueIdentifier,
                      @JsonProperty("credentials") Credentials credentials) {
        this.uniqueIdentifier = uniqueIdentifier;
        this.credentials = credentials;
    }

    public UniqueIdentifier getIdentifier() {
        return uniqueIdentifier;
    }

    public Credentials getCredentials() {
        return credentials;
    }
}
