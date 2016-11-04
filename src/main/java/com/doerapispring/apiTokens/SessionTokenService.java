package com.doerapispring.apiTokens;

import com.doerapispring.Identifier;
import com.doerapispring.users.RegisteredUser;
import com.doerapispring.users.UserEntity;
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

    public SessionToken getActive(String userEmail) {
        SessionTokenEntity sessionTokenEntity = sessionTokenRepository.findActiveByUserEmail(userEmail);
        if (sessionTokenEntity == null) return null;
        return SessionToken.builder()
                .token(sessionTokenEntity.token)
                .expiresAt(sessionTokenEntity.expiresAt)
                .build();
    }

    // TODO: See how often this is used. Maybe those places should have a direct
    // dependency on session token repo? This breaks the service pattern
    public SessionTokenEntity getByToken(String token) {
        return sessionTokenRepository.findFirstByTokenAndExpiresAtAfter(token, new Date());
    }

    public void expire(String userEmail) {
        SessionTokenEntity sessionTokenEntity = sessionTokenRepository.findActiveByUserEmail(userEmail);
        if (sessionTokenEntity != null) {
            sessionTokenEntity.expiresAt = new Date();
            sessionTokenRepository.save(sessionTokenEntity);
        }
    }

    public UserSession start(RegisteredUser registeredUser) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(DATE, 7);
        UserSession userSession = new UserSession(registeredUser.getEmail(),
                tokenGenerator.generate(),
                calendar.getTime());
        newSessionTokenRepository.add(userSession);
        return userSession;
    }

    public SessionToken grant(Identifier identifier) {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(DATE, 7);
        SessionToken sessionToken = SessionToken.builder()
                .identifier(identifier)
                .token(tokenGenerator.generate())
                .expiresAt(calendar.getTime())
                .build();
        newSessionTokenRepository.add(sessionToken);
        return sessionToken;
    }
}
