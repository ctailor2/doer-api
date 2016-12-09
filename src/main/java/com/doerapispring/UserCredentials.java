package com.doerapispring;

/**
 * Created by chiragtailor on 11/5/16.
 */
public class UserCredentials {
    private final UserIdentifier userIdentifier;
    private final EncodedCredentials encodedCredentials;

    public UserCredentials(UserIdentifier userIdentifier,
                           EncodedCredentials encodedCredentials) {
        this.userIdentifier = userIdentifier;
        this.encodedCredentials = encodedCredentials;
    }

    public UserIdentifier getUserIdentifier() {
        return userIdentifier;
    }

    public EncodedCredentials getEncodedCredentials() {
        return encodedCredentials;
    }
}