package com.doerapispring.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.access.channel.ChannelProcessingFilter;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AuthenticationProvider authenticationProvider;

    static final String SESSION_TOKEN_HEADER = "Session-Token";

    private static final String API_ROOT_ENDPOINT = "/v1/";
    private static final String BASE_RESOURCES_ENDPOINT = "/v1/resources/base";
    private static final String SIGNUP_ENDPOINT = "/v1/signup";
    private static final String LOGIN_ENDPOINT = "/v1/login";
    private static final String TOKEN_AUTH_ENDPOINT = "/v1/**";

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Override
    public void configure(WebSecurity webSecurity) throws Exception {
        webSecurity
            .ignoring()
            .antMatchers(HttpMethod.OPTIONS);
    }

    @Override
    public void configure(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
            .csrf().disable()
            .authorizeRequests()
            .antMatchers(API_ROOT_ENDPOINT).permitAll()
            .antMatchers(BASE_RESOURCES_ENDPOINT).permitAll()
            .antMatchers(SIGNUP_ENDPOINT).permitAll()
            .antMatchers(LOGIN_ENDPOINT).permitAll()
            .and()
            .authorizeRequests()
            .antMatchers(TOKEN_AUTH_ENDPOINT).authenticated()
            .and()
            .addFilterBefore(buildTokenAuthenticationProcessingFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(new WebSecurityCorsFilter(), ChannelProcessingFilter.class);
    }

    @Bean
    TokenAuthenticationFilter buildTokenAuthenticationProcessingFilter() {
        List<String> pathsToSkip = Arrays.asList(API_ROOT_ENDPOINT, BASE_RESOURCES_ENDPOINT, SIGNUP_ENDPOINT, LOGIN_ENDPOINT);
        SkipAuthenticationPathRequestMatcher matcher = new SkipAuthenticationPathRequestMatcher(pathsToSkip, TOKEN_AUTH_ENDPOINT);
        TokenAuthenticationFilter filter = new TokenAuthenticationFilter(matcher);
        filter.setAuthenticationManager(this.authenticationManager);
        return filter;
    }

    @Bean
    @Override
    public AuthenticationManager authenticationManagerBean() throws Exception {
        return super.authenticationManagerBean();
    }

    protected void configure(AuthenticationManagerBuilder auth) {
        auth.authenticationProvider(authenticationProvider);
    }
}
