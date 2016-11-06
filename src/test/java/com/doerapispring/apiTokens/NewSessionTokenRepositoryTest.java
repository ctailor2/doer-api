package com.doerapispring.apiTokens;

import com.doerapispring.UserIdentifier;
import com.doerapispring.users.UserDAO;
import com.doerapispring.users.UserEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by chiragtailor on 10/26/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class NewSessionTokenRepositoryTest {
    private NewSessionTokenRepository newSessionTokenRepository;

    @Mock
    private UserDAO userDAO;

    @Mock
    private SessionTokenDAO sessionTokenDAO;

    @Captor
    ArgumentCaptor<SessionTokenEntity> sessionTokenEntityArgumentCaptor;

    @Before
    public void setUp() throws Exception {
        newSessionTokenRepository = new NewSessionTokenRepository(userDAO, sessionTokenDAO);
    }

    @Test
    public void add_sessionToken_findsUser_savesRelationship_savesFields_setsAuditingData() throws Exception {
        UserEntity userEntity = UserEntity.builder().build();
        when(userDAO.findByEmail(any())).thenReturn(userEntity);

        Date expiresAt = new Date();
        SessionToken sessionToken = SessionToken.builder()
                .userIdentifier(new UserIdentifier("soUnique"))
                .token("soRandomToken")
                .expiresAt(expiresAt)
                .build();

        newSessionTokenRepository.add(sessionToken);

        verify(sessionTokenDAO).save(sessionTokenEntityArgumentCaptor.capture());
        SessionTokenEntity sessionTokenEntity = sessionTokenEntityArgumentCaptor.getValue();
        assertThat(sessionTokenEntity.userEntity).isEqualTo(userEntity);
        assertThat(sessionTokenEntity.token).isEqualTo("soRandomToken");
        assertThat(sessionTokenEntity.expiresAt).isEqualTo(expiresAt);
        assertThat(sessionTokenEntity.createdAt).isToday();
        assertThat(sessionTokenEntity.updatedAt).isToday();
    }
}