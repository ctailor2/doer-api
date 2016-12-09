package com.doerapispring.apiTokens;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;

@RunWith(MockitoJUnitRunner.class)
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