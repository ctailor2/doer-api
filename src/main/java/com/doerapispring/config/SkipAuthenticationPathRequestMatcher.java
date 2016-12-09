package com.doerapispring.config;

import org.springframework.http.HttpMethod;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.OrRequestMatcher;
import org.springframework.security.web.util.matcher.RequestMatcher;
import org.springframework.util.Assert;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

public class SkipAuthenticationPathRequestMatcher implements RequestMatcher {
    private final OrRequestMatcher matchers;
    private final AntPathRequestMatcher processingMatcher;

    public SkipAuthenticationPathRequestMatcher(List<String> pathsToSkip, String processingPath) {
        Assert.notNull(pathsToSkip);
        List<RequestMatcher> requestMatchers = pathsToSkip.stream().map(path -> new AntPathRequestMatcher(path)).collect(Collectors.toList());
        requestMatchers.add(new AntPathRequestMatcher(processingPath, HttpMethod.OPTIONS.toString()));
        matchers = new OrRequestMatcher(requestMatchers);
        processingMatcher = new AntPathRequestMatcher(processingPath);
    }

    @Override
    public boolean matches(HttpServletRequest request) {
        if(matchers.matches(request)) {
            return false;
        }
        return processingMatcher.matches(request);
    }
}
