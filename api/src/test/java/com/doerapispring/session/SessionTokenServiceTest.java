package com.doerapispring.session;

import com.doerapispring.authentication.TokenRefusedException;
import com.doerapispring.authentication.TransientAccessToken;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.mockito.ArgumentCaptor;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

public class SessionTokenServiceTest {
    private SessionTokenService sessionTokenService;

    private TokenGenerator tokenGenerator;

    private TokenStore sessionTokenRepository;

    private final ArgumentCaptor<TransientAccessToken> tokenArgumentCaptor = ArgumentCaptor.forClass(TransientAccessToken.class);

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        tokenGenerator = mock(TokenGenerator.class);
        sessionTokenRepository = mock(TokenStore.class);
        sessionTokenService = new SessionTokenService(
            tokenGenerator,
            sessionTokenRepository);
    }

    @Test
    public void getByToken_callsSessionTokenRepository() throws Exception {
        sessionTokenService.retrieve("token");

        verify(sessionTokenRepository).find("token");
    }

    @Test
    public void grant_generatesAccessIdentifier() throws Exception {
        sessionTokenService.grant("soUnique");

        verify(tokenGenerator).generate();
    }

    @Test
    public void grant_addsSessionTokenToRepository_returnsSessionToken() throws Exception {
        String accessToken = "thisIsYourToken";
        when(tokenGenerator.generate()).thenReturn(accessToken);

        String userIdentifier = "soUnique";
        TransientAccessToken grantedToken = sessionTokenService.grant(userIdentifier);

        verify(sessionTokenRepository).add(tokenArgumentCaptor.capture());
        TransientAccessToken token = tokenArgumentCaptor.getValue();
        assertThat(token.getAuthenticatedEntityIdentifier()).isEqualTo(userIdentifier);
        assertThat(token.getAccessToken()).isEqualTo(accessToken);
        assertThat(token.getExpiresAt()).isInTheFuture();
        assertThat(grantedToken).isNotNull();
    }

    @Test
    public void grant_whenTokenStoreRejects_refusesGrant() throws Exception {
        doThrow(InvalidTokenException.class).when(sessionTokenRepository).add(any());

        exception.expect(TokenRefusedException.class);
        sessionTokenService.grant("soUnique");
    }
}