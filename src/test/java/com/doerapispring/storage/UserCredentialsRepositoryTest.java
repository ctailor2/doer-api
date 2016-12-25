package com.doerapispring.storage;

import com.doerapispring.authentication.EncodedCredentials;
import com.doerapispring.authentication.UserCredentials;
import com.doerapispring.domain.ObjectRepository;
import com.doerapispring.domain.UserIdentifier;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Optional;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserCredentialsRepositoryTest {
    private ObjectRepository<UserCredentials, String> userCredentialsRepository;

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

    @Test
    public void find_callsUserDAO() throws Exception {
        userCredentialsRepository.find(new UserIdentifier("test"));

        verify(userDAO).findByEmail("test");
    }

    @Test
    public void find_whenUserFound_returnsOptionalWithUserCredentials() throws Exception {
        UserEntity userEntity = UserEntity.builder()
                .passwordDigest("securePassword")
                .build();
        when(userDAO.findByEmail(any())).thenReturn(userEntity);

        UserIdentifier userIdentifier = new UserIdentifier("test");
        Optional<UserCredentials> userCredentialsOptional = userCredentialsRepository.find(userIdentifier);

        assertThat(userCredentialsOptional.isPresent()).isTrue();
        assertThat(userCredentialsOptional.get().getIdentifier()).isEqualTo(userIdentifier);
        assertThat(userCredentialsOptional.get().getEncodedCredentials()).isEqualTo(new EncodedCredentials("securePassword"));
    }

    @Test
    public void find_whenUserNotFound_returnsNull() throws Exception {
        when(userDAO.findByEmail(any())).thenReturn(null);

        UserIdentifier userIdentifier = new UserIdentifier("test");
        Optional<UserCredentials> userCredentialsOptional = userCredentialsRepository.find(userIdentifier);

        assertThat(userCredentialsOptional.isPresent()).isFalse();
    }
}