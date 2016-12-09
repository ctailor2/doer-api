package com.doerapispring.apiTokens;

import com.doerapispring.AbnormalModelException;
import com.doerapispring.UserIdentifier;
import com.doerapispring.userSessions.OperationRefusedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import static java.util.Calendar.DATE;

@Service
@Transactional
public class SessionTokenService {
    private TokenGenerator tokenGenerator;
    private SessionTokenRepository sessionTokenRepository;

    @Autowired
    public SessionTokenService(TokenGenerator tokenGenerator,
                               SessionTokenRepository sessionTokenRepository) {
        this.tokenGenerator = tokenGenerator;
        this.sessionTokenRepository = sessionTokenRepository;
    }

    // TODO: See how often this is used. Maybe those places should have a direct
    // dependency on session token repo? This breaks the service pattern
    public Optional<SessionToken> getByToken(SessionTokenIdentifier sessionTokenIdentifier) {
        return sessionTokenRepository.find(sessionTokenIdentifier);
    }

    public SessionToken grant(UserIdentifier userIdentifier) throws OperationRefusedException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(DATE, 7);
        SessionToken sessionToken = SessionToken.builder()
                .userIdentifier(userIdentifier)
                .token(tokenGenerator.generate())
                .expiresAt(calendar.getTime())
                .build();
        try {
            sessionTokenRepository.add(sessionToken);
        } catch (AbnormalModelException e) {
            throw new OperationRefusedException();
        }
        return sessionToken;
    }
}
