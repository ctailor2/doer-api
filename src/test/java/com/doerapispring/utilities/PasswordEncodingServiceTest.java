package com.doerapispring.utilities;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.verify;

/**
 * Created by chiragtailor on 10/24/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class PasswordEncodingServiceTest {
    private PasswordEncodingService passwordEncodingService;

    @Mock
    PasswordEncoder passwordEncoder;

    @Before
    public void setUp() throws Exception {
        passwordEncodingService = new PasswordEncodingService(passwordEncoder);
    }

    @Test
    public void encode_callsPasswordEncoder() throws Exception {
        passwordEncodingService.encode("somePassword");

        verify(passwordEncoder).encode("somePassword");
    }
}