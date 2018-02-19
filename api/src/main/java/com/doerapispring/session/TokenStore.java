package com.doerapispring.session;

import com.doerapispring.authentication.TransientAccessToken;

import java.util.Optional;

public interface TokenStore {
    void add(TransientAccessToken transientAccessToken) throws InvalidTokenException;

    Optional<TransientAccessToken> find(String accessToken);
}
