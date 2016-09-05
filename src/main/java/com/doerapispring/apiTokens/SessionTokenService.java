package com.doerapispring.apiTokens;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;

import static java.util.Calendar.DATE;

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
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(DATE, 7);
        SessionToken sessionToken = SessionToken.builder()
                .userId(userId)
                .token(tokenGenerator.generate())
                .expiresAt(calendar.getTime())
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        return sessionTokenRepository.save(sessionToken);
    }

    public SessionToken get(long userId) {
        return sessionTokenRepository.findFirstByUserIdAndExpiresAtAfter(userId, new Date());
    }
}
