package com.doerapispring.authentication;

import java.util.Date;

public class Credentials {
    private String userIdentifier;
    private String secret;
    private Date effectiveAt;

    public Credentials(String userIdentifier,
                       String secret,
                       Date effectiveAt) {
        this.userIdentifier = userIdentifier;
        this.secret = secret;
        this.effectiveAt = effectiveAt;
    }

    public String getUserIdentifier() {
        return userIdentifier;
    }

    public String getSecret() {
        return secret;
    }
}
