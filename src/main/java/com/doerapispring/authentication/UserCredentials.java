package com.doerapispring.authentication;

import com.doerapispring.domain.UniquelyIdentifiable;
import com.doerapispring.domain.UserIdentifier;

public class UserCredentials implements UniquelyIdentifiable {
    private final UserIdentifier userIdentifier;
    private final EncodedCredentials encodedCredentials;

    public UserCredentials(UserIdentifier userIdentifier,
                           EncodedCredentials encodedCredentials) {
        this.userIdentifier = userIdentifier;
        this.encodedCredentials = encodedCredentials;
    }

    public UserIdentifier getIdentifier() {
        return userIdentifier;
    }

    public EncodedCredentials getEncodedCredentials() {
        return encodedCredentials;
    }
}
