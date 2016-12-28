package com.doerapispring.authentication;

import com.doerapispring.domain.UniqueIdentifier;
import com.doerapispring.domain.UniqueIdentifier;

public class AuthenticatedUser {
    private final UniqueIdentifier uniqueIdentifier;

    public static AuthenticatedUser identifiedWith(UniqueIdentifier uniqueIdentifier) {
        return new AuthenticatedUser(uniqueIdentifier);
    }

    public AuthenticatedUser(UniqueIdentifier uniqueIdentifier) {
        this.uniqueIdentifier = uniqueIdentifier;
    }

    public UniqueIdentifier getUserIdentifier() {
        return uniqueIdentifier;
    }
}
