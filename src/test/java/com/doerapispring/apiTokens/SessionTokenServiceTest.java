package com.doerapispring.apiTokens;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

/**
 * Created by chiragtailor on 8/25/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class SessionTokenServiceTest {
    private SessionTokenService sessionTokenService;

    @Mock
    private SessionTokenRepository sessionTokenRepository;

    @Mock
    private TokenGenerator tokenGenerator;

    private ArgumentCaptor<SessionToken> sessionTokenArgumentCaptor = ArgumentCaptor.forClass(SessionToken.class);

    @Before
    public void setUp() throws Exception {
        sessionTokenService = new SessionTokenService(sessionTokenRepository, tokenGenerator);
    }

    @Test
    public void create_callsTokenGenerator_callsSessionTokenRepository_setsFields() throws Exception {
        doReturn("sorandomtoken").when(tokenGenerator).generate();

        sessionTokenService.create(1L);

        verify(tokenGenerator).generate();
        verify(sessionTokenRepository).save(sessionTokenArgumentCaptor.capture());
        SessionToken savedSessionToken = sessionTokenArgumentCaptor.getValue();
        assertThat(savedSessionToken.userId).isEqualTo(1L);
        assertThat(savedSessionToken.token).isEqualTo("sorandomtoken");
    }

    @Test
    public void create_callsSessionTokenRepository_defaultsFields() throws Exception {
        sessionTokenService.create(1L);

        verify(sessionTokenRepository).save(sessionTokenArgumentCaptor.capture());
        SessionToken savedSessionToken = sessionTokenArgumentCaptor.getValue();
        assertThat(savedSessionToken.expiresAt).isToday();
    }

    @Test
    public void create_callsSessionTokenRepository_setsAuditingData() throws Exception {
        sessionTokenService.create(1L);

        verify(sessionTokenRepository).save(sessionTokenArgumentCaptor.capture());
        SessionToken savedSessionToken = sessionTokenArgumentCaptor.getValue();
        assertThat(savedSessionToken.createdAt).isToday();
        assertThat(savedSessionToken.updatedAt).isToday();
    }
}