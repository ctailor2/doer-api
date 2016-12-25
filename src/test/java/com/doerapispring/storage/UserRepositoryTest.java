package com.doerapispring.storage;

import com.doerapispring.domain.ObjectRepository;
import com.doerapispring.domain.User;
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
public class UserRepositoryTest {
    private ObjectRepository<User, String> userRepository;

    @Mock
    private UserDAO userDAO;

    @Captor
    ArgumentCaptor<UserEntity> userEntityArgumentCaptor;

    @Before
    public void setUp() throws Exception {
        userRepository = new UserRepository(userDAO);
    }

    @Test
    public void add_user_callsUserDao_savesFields_setsAuditingData_addsEmptyPassword_returnsUser() throws Exception {
        User user = new User(new UserIdentifier("soUnique"));

        userRepository.add(user);

        verify(userDAO).save(userEntityArgumentCaptor.capture());
        UserEntity userEntity = userEntityArgumentCaptor.getValue();
        assertThat(userEntity.email).isEqualTo("soUnique");
        assertThat(userEntity.passwordDigest).isEmpty();
        assertThat(userEntity.createdAt).isToday();
        assertThat(userEntity.updatedAt).isToday();
    }

    @Test
    public void find_callsUserDao_whenUserFound_returnsOptionalWithUser() throws Exception {
        UserEntity userEntity = UserEntity.builder().build();
        when(userDAO.findByEmail(any())).thenReturn(userEntity);

        UserIdentifier userIdentifier = new UserIdentifier("soUnique");
        Optional<User> userOptional = userRepository.find(userIdentifier);

        verify(userDAO).findByEmail("soUnique");
        assertThat(userOptional.isPresent()).isTrue();
        assertThat(userOptional.get().getIdentifier()).isEqualTo(userIdentifier);
    }

    @Test
    public void find_callsUserDao_whenUserNotFound_returnsEmptyOptional() throws Exception {
        when(userDAO.findByEmail(any())).thenReturn(null);

        Optional<User> userOptional = userRepository.find(new UserIdentifier("soUnique"));

        verify(userDAO).findByEmail("soUnique");
        assertThat(userOptional.isPresent()).isFalse();

    }
}