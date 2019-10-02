package com.doerapispring.session;

import org.junit.Before;
import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TokenGeneratorTest {
    private TokenGenerator tokenGenerator;

    @Before
    public void setUp() throws Exception {
        tokenGenerator = new TokenGenerator();
    }

    @Test
    public void generate_generatesTokenString_with32Characters_andNoSpaces() throws Exception {
        String token = tokenGenerator.generate();
        assertThat(token.length()).isEqualTo(32);
        assertThat(token.matches("^[^\\s]+$"));
    }
}