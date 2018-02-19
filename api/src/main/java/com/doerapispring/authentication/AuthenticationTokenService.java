package com.doerapispring.authentication;

import java.util.Optional;

public interface AuthenticationTokenService {
    TransientAccessToken grant(String userIdentifier) throws TokenRefusedException;

    Optional<TransientAccessToken> retrieve(String accessToken);
}
