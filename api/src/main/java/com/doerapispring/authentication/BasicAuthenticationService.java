package com.doerapispring.authentication;

public interface BasicAuthenticationService {
    void registerCredentials(String userIdentifier, String credentials) throws CredentialsInvalidException;

    boolean authenticate(String userIdentifier, String credentials);
}
