package com.doerapispring.session;

import com.doerapispring.authentication.AuthenticationTokenService;
import com.doerapispring.authentication.TokenRefusedException;
import com.doerapispring.authentication.TransientAccessToken;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Calendar;
import java.util.Date;
import java.util.Optional;

import static java.util.Calendar.DATE;

@Service
@Transactional
public class SessionTokenService implements AuthenticationTokenService {
    private final TokenGenerator tokenGenerator;
    private final TokenStore tokenStore;

    SessionTokenService(TokenGenerator tokenGenerator,
                        TokenStore tokenStore) {
        this.tokenGenerator = tokenGenerator;
        this.tokenStore = tokenStore;
    }

    public Optional<TransientAccessToken> retrieve(String accessToken) {
        return tokenStore.find(accessToken);
    }

    public TransientAccessToken grant(String userIdentifier) throws TokenRefusedException {
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        calendar.add(DATE, 7);
        SessionToken sessionToken = new SessionToken(userIdentifier,
                tokenGenerator.generate(),
                calendar.getTime());
        try {
            tokenStore.add(sessionToken);
        } catch (InvalidTokenException e) {
            throw new TokenRefusedException();
        }
        return sessionToken;
    }
}
