package com.doerapispring.session;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
class TokenGenerator {
    private final SecureRandom random = new SecureRandom();
    private final Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    String generate() {
        byte bytes[] = new byte[24];
        random.nextBytes(bytes);
        return encoder.encodeToString(bytes);
    }
}
