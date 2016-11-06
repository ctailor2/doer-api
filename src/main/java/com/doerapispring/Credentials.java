package com.doerapispring;

/**
 * Created by chiragtailor on 11/3/16.
 */
public class Credentials {
    private final String credentials;

    public Credentials(String credentials) {
        this.credentials = credentials;
    }

    public String get() {
        return credentials;
    }
}
