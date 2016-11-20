package com.doerapispring.apiTokens;

import com.doerapispring.UserIdentifier;
import com.doerapispring.users.UserEntity;
import com.doerapispring.users.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

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
    private NewSessionTokenRepository newSessionTokenRepository;

    @Autowired
    public SessionTokenService(SessionTokenRepository sessionTokenRepository,
                               TokenGenerator tokenGenerator,
                               UserRepository userRepository,
                               NewSessionTokenRepository newSessionTokenRepository) {
        this.sessionTokenRepository = sessionTokenRepository;
        this.tokenGenerator = tokenGenerator;
        this.userRepository = userRepository;
        this.newSessionTokenRepository = newSessionTokenRepository;
    }

    public SessionToken create(String userEmail) {
        UserEntity userEntity = userRepository.findByEmail(userEmail);
        if (userEntity == null) return null;
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(DATE, 7);
        SessionTokenEntity sessionTokenEntity = SessionTokenEntity.builder()
                .userEntity(userEntity)
                .token(tokenGenerator.generate())
                .expiresAt(calendar.getTime())
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        sessionTokenRepository.save(sessionTokenEntity);
        return SessionToken.builder()
                .token(sessionTokenEntity.token)
                .expiresAt(sessionTokenEntity.expiresAt)
                .build();
    }

    // TODO: See how often this is used. Maybe those places should have a direct
    // dependency on session token repo? This breaks the service pattern
    public Optional<SessionToken> getByToken(SessionTokenIdentifier sessionTokenIdentifier) {
        return newSessionTokenRepository.find(sessionTokenIdentifier);
    }

    public SessionToken grant(UserIdentifier userIdentifier) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(DATE, 7);
        SessionToken sessionToken = SessionToken.builder()
                .userIdentifier(userIdentifier)
                .token(tokenGenerator.generate())
                .expiresAt(calendar.getTime())
                .build();
        newSessionTokenRepository.add(sessionToken);
        return sessionToken;
    }
}
