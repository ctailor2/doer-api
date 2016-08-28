package com.doerapispring.apiTokens;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

/**
 * Created by chiragtailor on 8/25/16.
 */
@Service
@Transactional
public class SessionTokenService {
    private SessionTokenRepository sessionTokenRepository;
    private TokenGenerator tokenGenerator;

    @Autowired
    public SessionTokenService(SessionTokenRepository sessionTokenRepository, TokenGenerator tokenGenerator) {
        this.sessionTokenRepository = sessionTokenRepository;
        this.tokenGenerator = tokenGenerator;
    }

    public SessionToken create(long userId) {
        SessionToken sessionToken = SessionToken.builder()
                .userId(userId)
                .token(tokenGenerator.generate())
                .expiresAt(new Date())
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        return sessionTokenRepository.save(sessionToken);
    }
}
