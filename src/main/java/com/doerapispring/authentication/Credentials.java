package com.doerapispring.authentication;

public class Credentials {
    private final String credentials;

    public Credentials(String credentials) {
        this.credentials = credentials;
    }

    public String get() {
        return credentials;
    }
}
