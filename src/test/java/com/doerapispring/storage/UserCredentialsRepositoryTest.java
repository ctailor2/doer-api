package com.doerapispring.storage;

import com.doerapispring.authentication.Credentials;
import com.doerapispring.authentication.CredentialsStore;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Date;
import java.util.Optional;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class UserCredentialsRepositoryTest {
    private CredentialsStore userCredentialsRepository;

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

        userCredentialsRepository.add(new Credentials("test@id.com", "soSecret", new Date()));

        verify(userDAO).findByEmail("test@id.com");
        verify(userDAO).save(userEntityArgumentCaptor.capture());
        UserEntity userEntity = userEntityArgumentCaptor.getValue();
        assertThat(userEntity.passwordDigest).isEqualTo("soSecret");
    }

    @Test
    public void findLatest_callsUserDAO() throws Exception {
        userCredentialsRepository.findLatest("test");

        verify(userDAO).findByEmail("test");
    }

    @Test
    public void findLatest_whenUserFound_returnsOptionalWithCredentials() throws Exception {
        UserEntity userEntity = UserEntity.builder()
                .passwordDigest("securePassword")
                .build();
        when(userDAO.findByEmail(any())).thenReturn(userEntity);

        String userIdentifier = "test";
        Optional<Credentials> credentialsOptional = userCredentialsRepository.findLatest(userIdentifier);

        assertThat(credentialsOptional.isPresent()).isTrue();
        assertThat(credentialsOptional.get().getUserIdentifier()).isEqualTo(userIdentifier);
        assertThat(credentialsOptional.get().getSecret()).isEqualTo("securePassword");
    }

    @Test
    public void findLatest_whenUserNotFound_returnsNull() throws Exception {
        when(userDAO.findByEmail(any())).thenReturn(null);

        Optional<Credentials> credentialsOptional = userCredentialsRepository.findLatest("test");

        assertThat(credentialsOptional.isPresent()).isFalse();
    }
}