package com.doerapispring.authentication;

import java.util.Optional;

public interface CredentialsStore {
    void add(Credentials credentials);

    Optional<Credentials> findLatest(String userIdentifier);
}
