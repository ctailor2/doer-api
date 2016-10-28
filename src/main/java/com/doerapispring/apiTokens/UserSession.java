package com.doerapispring.apiTokens;

import java.util.Date;

/**
 * Created by chiragtailor on 10/26/16.
 */
public class UserSession {
    private final String email;
    private final String token;
    private final Date expiresAt;

    public UserSession(String email, String token, Date expiresAt) {

        this.email = email;
        this.token = token;
        this.expiresAt = expiresAt;
    }

    public String getEmail() {
        return email;
    }

    public String getToken() {
        return token;
    }

    public Date getExpiresAt() {
        return expiresAt;
    }
}
