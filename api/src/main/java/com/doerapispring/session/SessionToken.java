package com.doerapispring.session;

import com.doerapispring.authentication.TransientAccessToken;

import java.util.Date;

public class SessionToken implements TransientAccessToken {
    private final String authenticatedEntityIdentifier;
    private final String accessToken;
    private final Date expiresAt;

    public SessionToken(String authenticatedEntityIdentifier, String accessToken, Date expiresAt) {
        this.authenticatedEntityIdentifier = authenticatedEntityIdentifier;
        this.accessToken = accessToken;
        this.expiresAt = expiresAt;
    }

    @Override
    public String getAccessToken() {
        return accessToken;
    }

    @Override
    public Date getExpiresAt() {
        return expiresAt;
    }

    @Override
    public String getAuthenticatedEntityIdentifier() {
        return authenticatedEntityIdentifier;
    }
}
