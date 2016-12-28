package com.doerapispring.authentication;

import com.doerapispring.domain.UniquelyIdentifiable;
import com.doerapispring.domain.UniqueIdentifier;

public class UserCredentials implements UniquelyIdentifiable<String> {
    private final UniqueIdentifier<String> uniqueIdentifier;
    private final EncodedCredentials encodedCredentials;

    public UserCredentials(UniqueIdentifier uniqueIdentifier,
                           EncodedCredentials encodedCredentials) {
        this.uniqueIdentifier = uniqueIdentifier;
        this.encodedCredentials = encodedCredentials;
    }

    public UniqueIdentifier<String> getIdentifier() {
        return uniqueIdentifier;
    }

    public EncodedCredentials getEncodedCredentials() {
        return encodedCredentials;
    }
}
