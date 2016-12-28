package com.doerapispring.authentication;

import com.doerapispring.domain.AbnormalModelException;
import com.doerapispring.domain.ObjectRepository;
import com.doerapispring.domain.OperationRefusedException;
import com.doerapispring.domain.UniqueIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import static java.util.Calendar.DATE;

@Service
@Transactional
class SessionTokenService {
    private TokenGenerator tokenGenerator;
    private ObjectRepository<SessionToken, String> sessionTokenRepository;

    @Autowired
    public SessionTokenService(TokenGenerator tokenGenerator,
                               ObjectRepository<SessionToken, String> sessionTokenRepository) {
        this.tokenGenerator = tokenGenerator;
        this.sessionTokenRepository = sessionTokenRepository;
    }

    // TODO: See how often this is used. Maybe those places should have a direct
    // dependency on session token repo? This breaks the service pattern
    public Optional<SessionToken> getByToken(UniqueIdentifier uniqueIdentifier) {
        return sessionTokenRepository.find(uniqueIdentifier);
    }

    public SessionToken grant(UniqueIdentifier uniqueIdentifier) throws OperationRefusedException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(DATE, 7);
        SessionToken sessionToken = SessionToken.builder()
                .userIdentifier(uniqueIdentifier)
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
