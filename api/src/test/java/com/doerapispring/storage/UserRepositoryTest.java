package com.doerapispring.storage;

import com.doerapispring.domain.ObjectRepository;
import com.doerapispring.domain.User;
import com.doerapispring.domain.UserId;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

public class UserRepositoryTest {
    private ObjectRepository<User, UserId> userRepository;

    private UserDAO userDAO;

    private final ArgumentCaptor<UserEntity> userEntityArgumentCaptor = ArgumentCaptor.forClass(UserEntity.class);

    @Before
    public void setUp() throws Exception {
        userDAO = mock(UserDAO.class);
        userRepository = new UserRepository(userDAO);
    }

    @Test
    public void saveUser_callsUserDao_savesFields_setsAuditingData_addsEmptyPassword_returnsUser() throws Exception {
        User user = new User(new UserId("soUnique"));

        userRepository.save(user);

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

        UserId userId = new UserId("soUnique");
        Optional<User> userOptional = userRepository.find(userId);

        verify(userDAO).findByEmail("soUnique");
        assertThat(userOptional.isPresent()).isTrue();
        assertThat(userOptional.get().getUserId()).isEqualTo(userId);
    }

    @Test
    public void find_callsUserDao_whenUserNotFound_returnsEmptyOptional() throws Exception {
        when(userDAO.findByEmail(any())).thenReturn(null);

        Optional<User> userOptional = userRepository.find(new UserId("soUnique"));

        verify(userDAO).findByEmail("soUnique");
        assertThat(userOptional.isPresent()).isFalse();

    }
}