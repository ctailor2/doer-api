package com.doerapispring.web;

import com.doerapispring.authentication.AuthenticatedAuthenticationToken;
import com.doerapispring.authentication.AuthenticatedUser;
import org.junit.Before;
import org.junit.Test;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthenticatedControllerTest {
    private MockMvc mockMvc;
    private AuthenticatedUser authenticatedUser;
    private AuthenticatedController authenticatedController;

    @RestController
    static class AuthenticatedController {
        @RequestMapping(value = "/authenticated-endpoint", method = RequestMethod.GET)
        @ResponseBody
        ResponseEntity<String> authenticatedEndpoint(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
            return ResponseEntity.ok(authenticatedUser.getIdentifier());
        }
    }

    @Before
    public void setUp() throws Exception {
        authenticatedUser = new AuthenticatedUser("someIdentifier", "someListId");
        SecurityContextHolder.getContext().setAuthentication(new AuthenticatedAuthenticationToken(authenticatedUser));
        authenticatedController = new AuthenticatedController();
        mockMvc = MockMvcBuilders
            .standaloneSetup(authenticatedController)
            .setCustomArgumentResolvers(new AuthenticationPrincipalArgumentResolver())
            .build();
    }

    @Test
    public void properlyResolvesTheAuthenticationPrincipal_fromTheAuthenticationSecurityContext() throws Exception {
        mockMvc.perform(get("/authenticated-endpoint"))
            .andExpect(status().isOk())
            .andExpect(content().string("someIdentifier"));
    }
}