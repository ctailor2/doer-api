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


/**
 * Created by chiragtailor on 8/17/16.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfiguration extends WebSecurityConfigurerAdapter {
    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private AuthenticationProvider authenticationProvider;

    public static final String SESSION_TOKEN_HEADER = "Session-Token";
    public static final String SIGNUP_ENDPOINT = "/v1/signup";
    public static final String LOGIN_ENDPOINT = "/v1/login";
    public static final String TOKEN_AUTH_ENDPOINT = "/v1/**";

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
    protected TokenAuthenticationFilter buildTokenAuthenticationProcessingFilter() throws Exception {
        List<String> pathsToSkip = Arrays.asList(SIGNUP_ENDPOINT, LOGIN_ENDPOINT);
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
