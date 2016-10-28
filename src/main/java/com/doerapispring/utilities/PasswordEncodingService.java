package com.doerapispring.utilities;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * Created by chiragtailor on 10/24/16.
 */
@Service
public class PasswordEncodingService {
    private PasswordEncoder passwordEncoder;

    @Autowired
    public PasswordEncodingService(PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
    }


    public String encode(String password) {
        return passwordEncoder.encode(password);
    }
}
