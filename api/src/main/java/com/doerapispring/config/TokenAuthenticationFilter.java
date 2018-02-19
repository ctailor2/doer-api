package com.doerapispring.config;

import com.doerapispring.authentication.PreAuthenticatedAuthenticationToken;
import com.doerapispring.web.Error;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

public class TokenAuthenticationFilter extends AbstractAuthenticationProcessingFilter {
    @Autowired
    protected TokenAuthenticationFilter(RequestMatcher requiresAuthenticationRequestMatcher) {
        super(requiresAuthenticationRequestMatcher);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response) throws AuthenticationException, IOException, ServletException {
        String token = request.getHeader(SecurityConfiguration.SESSION_TOKEN_HEADER);
        if (token == null) throw new AuthenticationCredentialsNotFoundException("Authentication required");
        PreAuthenticatedAuthenticationToken preAuthenticatedAuthenticationToken = new PreAuthenticatedAuthenticationToken(token);
        return getAuthenticationManager().authenticate(preAuthenticatedAuthenticationToken);
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request, HttpServletResponse response, FilterChain chain, Authentication authResult) throws IOException, ServletException {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authResult);
        SecurityContextHolder.setContext(context);
        chain.doFilter(request, response);
    }

    @Override
    protected void unsuccessfulAuthentication(HttpServletRequest request, HttpServletResponse response, AuthenticationException failed) throws IOException, ServletException {
        SecurityContextHolder.clearContext();
        ObjectMapper mapper = new ObjectMapper();
        Error error = Error.builder()
                .status("401")
                .message(failed.getMessage())
                .build();
        String serializedError = mapper.writeValueAsString(error);
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.getWriter().write(serializedError);
    }
}
