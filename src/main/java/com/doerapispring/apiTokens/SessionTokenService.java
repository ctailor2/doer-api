package com.doerapispring.apiTokens;

import com.doerapispring.users.User;
import com.doerapispring.users.UserRepository;
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
    private UserRepository userRepository;

    @Autowired
    public SessionTokenService(SessionTokenRepository sessionTokenRepository, TokenGenerator tokenGenerator, UserRepository userRepository) {
        this.sessionTokenRepository = sessionTokenRepository;
        this.tokenGenerator = tokenGenerator;
        this.userRepository = userRepository;
    }

    public SessionTokenEntity create(String userEmail) {
        User user = userRepository.findByEmail(userEmail);
        if (user == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(DATE, 7);
        SessionToken sessionToken = SessionToken.builder()
                .user(user)
                .token(tokenGenerator.generate())
                .expiresAt(calendar.getTime())
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        sessionTokenRepository.save(sessionToken);
        return SessionTokenEntity.builder()
                .token(sessionToken.token)
                .build();
    }

    public SessionTokenEntity getActive(String userEmail) {
        SessionToken sessionToken = sessionTokenRepository.findActiveByUserEmail(userEmail);
        return SessionTokenEntity.builder()
                .token(sessionToken.token)
                .build();
    }

    // TODO: This was mostly being used before the auth filter was introduced - refactor it away
    public SessionToken getByToken(String token) {
        return sessionTokenRepository.findFirstByTokenAndExpiresAtAfter(token, new Date());
    }

    public void expire(String token) {
        SessionToken sessionToken = getByToken(token);
        if (sessionToken != null) {
            sessionToken.expiresAt = new Date();
            sessionTokenRepository.save(sessionToken);
        }
    }
}
