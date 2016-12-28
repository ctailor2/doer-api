package com.doerapispring.web;

import com.doerapispring.authentication.Credentials;
import com.doerapispring.domain.UniqueIdentifier;
import com.doerapispring.domain.UniqueIdentifier;
import com.fasterxml.jackson.annotation.JsonProperty;

public class LoginForm {
    private final UniqueIdentifier uniqueIdentifier;
    private final Credentials credentials;

    public LoginForm(@JsonProperty("identifier") UniqueIdentifier uniqueIdentifier,
                     @JsonProperty("credentials") Credentials credentials) {
        this.uniqueIdentifier = uniqueIdentifier;
        this.credentials = credentials;
    }

    public UniqueIdentifier getUserIdentifier() {
        return uniqueIdentifier;
    }

    public Credentials getCredentials() {
        return credentials;
    }
}
