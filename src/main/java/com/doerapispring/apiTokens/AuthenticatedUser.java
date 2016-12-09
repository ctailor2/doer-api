package com.doerapispring.apiTokens;

import com.doerapispring.UserIdentifier;

public class AuthenticatedUser {
    private final UserIdentifier userIdentifier;

    public static AuthenticatedUser identifiedWith(UserIdentifier userIdentifier) {
        return new AuthenticatedUser(userIdentifier);
    }

    public AuthenticatedUser(UserIdentifier userIdentifier) {
        this.userIdentifier = userIdentifier;
    }

    public UserIdentifier getUserIdentifier() {
        return userIdentifier;
    }
}
