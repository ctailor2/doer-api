package com.doerapispring.storage;

import com.doerapispring.authentication.Credentials;
import com.doerapispring.authentication.CredentialsStore;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Date;
import java.util.Optional;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class UserCredentialsRepositoryTest {
    private CredentialsStore userCredentialsRepository;

    private UserDAO userDAO;

    private ArgumentCaptor<UserEntity> userEntityArgumentCaptor = ArgumentCaptor.forClass(UserEntity.class);

    @Before
    public void setUp() throws Exception {
        userDAO = mock(UserDAO.class);
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