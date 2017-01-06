package com.doerapispring.web;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.Date;

public class SessionTokenDTO {
    private final String token;
    private final Date expiresAt;

    @JsonCreator
    public SessionTokenDTO(@JsonProperty("token") String token,
                           @JsonProperty("expiresAt") Date expiresAt) {
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public String getToken() {
        return token;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }
}
