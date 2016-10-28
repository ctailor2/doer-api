package com.doerapispring.users;

/**
 * Created by chiragtailor on 10/24/16.
 */
public class RegisteredUser {
    private final String email;
    private final String encodedPassword;

    public RegisteredUser(String email, String encodedPassword) {
        this.email = email;
        this.encodedPassword = encodedPassword;
    }

    public String getEmail() {
        return email;
    }

    public String getEncodedPassword() {
        return encodedPassword;
    }
}
