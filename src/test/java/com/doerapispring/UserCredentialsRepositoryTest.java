package com.doerapispring;

import com.doerapispring.users.UserDAO;
import com.doerapispring.users.UserEntity;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Created by chiragtailor on 11/5/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class UserCredentialsRepositoryTest {
    private UserCredentialsRepository userCredentialsRepository;

    @Mock
    private UserDAO userDAO;

    @Captor
    private ArgumentCaptor<UserEntity> userEntityArgumentCaptor;

    @Before
    public void setUp() throws Exception {
        userCredentialsRepository = new UserCredentialsRepository(userDAO);
    }

    @Test
    public void add_callsUserDAO_findsUser_setsPassword() throws Exception {
        when(userDAO.findByEmail(any())).thenReturn(UserEntity.builder().build());

        UserCredentials userCredentials = new UserCredentials(
                new UserIdentifier("test@id.com"),
                new EncodedCredentials("soSecret"));
        userCredentialsRepository.add(userCredentials);

        verify(userDAO).findByEmail("test@id.com");
        verify(userDAO).save(userEntityArgumentCaptor.capture());
        UserEntity userEntity = userEntityArgumentCaptor.getValue();
        assertThat(userEntity.passwordDigest).isEqualTo("soSecret");
    }
}