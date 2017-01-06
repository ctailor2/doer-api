package com.doerapispring.authentication;

import java.util.Date;

public interface TransientAccessToken {
    String getAuthenticatedEntityIdentifier();

    String getAccessToken();

    Date getExpiresAt();
}
