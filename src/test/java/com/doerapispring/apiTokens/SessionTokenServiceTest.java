package com.doerapispring.apiTokens;

import com.doerapispring.AbnormalModelException;
import com.doerapispring.UserIdentifier;
import com.doerapispring.userSessions.OperationRefusedException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

/**
 * Created by chiragtailor on 8/25/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class SessionTokenServiceTest {
    private SessionTokenService sessionTokenService;

    @Mock
    private TokenGenerator tokenGenerator;

    @Mock
    private SessionTokenRepository sessionTokenRepository;

    @Captor
    private ArgumentCaptor<SessionToken> sessionTokenArgumentCaptor;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        sessionTokenService = new SessionTokenService(tokenGenerator,
                sessionTokenRepository);
    }

    @Test
    public void getByToken_callsSessionTokenRepository() throws Exception {
        SessionTokenIdentifier sessionTokenIdentifier = new SessionTokenIdentifier("token");
        sessionTokenService.getByToken(sessionTokenIdentifier);

        verify(sessionTokenRepository).find(sessionTokenIdentifier);
    }

    @Test
    public void grant_generatesAccessIdentifier() throws Exception {
        sessionTokenService.grant(new UserIdentifier("soUnique"));

        verify(tokenGenerator).generate();
    }

    @Test
    public void grant_addsSessionTokenToRepository_returnsSessionToken() throws Exception {
        when(tokenGenerator.generate()).thenReturn("thisIsYourToken");

        UserIdentifier userIdentifier = new UserIdentifier("soUnique");
        SessionToken grantedSessionToken = sessionTokenService.grant(userIdentifier);

        verify(sessionTokenRepository).add(sessionTokenArgumentCaptor.capture());
        SessionToken addedSessionToken = sessionTokenArgumentCaptor.getValue();
        assertThat(addedSessionToken.getUserIdentifier()).isEqualTo(userIdentifier);
        assertThat(addedSessionToken.getToken()).isEqualTo("thisIsYourToken");
        assertThat(addedSessionToken.getExpiresAt()).isInTheFuture();
        assertThat(grantedSessionToken).isNotNull();
    }

    @Test
    public void grant_whenRepositoryRejectsModel_refusesGrant() throws Exception {
        doThrow(AbnormalModelException.class).when(sessionTokenRepository).add(any());

        exception.expect(OperationRefusedException.class);
        sessionTokenService.grant(new UserIdentifier("soUnique"));
    }
}