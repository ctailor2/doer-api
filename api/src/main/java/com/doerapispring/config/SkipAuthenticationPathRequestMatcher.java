package com.doerapispring.config;

import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

public class SkipAuthenticationPathRequestMatcher implements RequestMatcher {
    private final OrRequestMatcher matchers;
    private final AntPathRequestMatcher processingMatcher;

    SkipAuthenticationPathRequestMatcher(List<String> pathsToSkip, String processingPath) {
        List<RequestMatcher> requestMatchers = pathsToSkip.stream().map(AntPathRequestMatcher::new).collect(Collectors.toList());
        requestMatchers.add(new AntPathRequestMatcher(processingPath, HttpMethod.OPTIONS.toString()));
        matchers = new OrRequestMatcher(requestMatchers);
        processingMatcher = new AntPathRequestMatcher(processingPath);
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        return !matchers.matches(request) && processingMatcher.matches(request);
    }
}
