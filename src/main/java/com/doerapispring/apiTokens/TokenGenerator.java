package com.doerapispring.apiTokens;

import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Base64;

@Component
class TokenGenerator {
    private SecureRandom random = new SecureRandom();
    private Base64.Encoder encoder = Base64.getUrlEncoder().withoutPadding();

    String generate() {
        byte bytes[] = new byte[24];
        random.nextBytes(bytes);
        return encoder.encodeToString(bytes);
    }
}
