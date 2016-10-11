package com.doerapispring.users;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;

/**
 * Created by chiragtailor on 8/18/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {
    private UserService userService;
    private User user;
    private UserEntity userEntity;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    private ArgumentCaptor<UserEntity> userArgumentCaptor = ArgumentCaptor.forClass(UserEntity.class);

    @Before
    public void setUp() throws Exception {
        userService = new UserService(userRepository, passwordEncoder);
        user = User.builder()
                .email("test@email.com")
                .password("password")
                .passwordConfirmation("password")
                .build();
        userEntity = UserEntity.builder()
                .id(123L)
                .build();
        doReturn("encodedPassword").when(passwordEncoder).encode(user.getPassword());
        doReturn(userEntity).when(userRepository).save(any(UserEntity.class));
    }

    @Test
    public void create_callsPasswordEncoder_callsUserRepository_setsFields_returnsUserEntity() throws Exception {
        User savedUser = userService.create(user);

        verify(passwordEncoder).encode(user.getPassword());
        verify(userRepository).save(userArgumentCaptor.capture());
        UserEntity savedUserEntity = userArgumentCaptor.getValue();
        assertThat(savedUserEntity.email).isEqualTo("test@email.com");
        assertThat(savedUserEntity.passwordDigest).isEqualTo("encodedPassword");
        assertThat(savedUserEntity.createdAt).isToday();
        assertThat(savedUserEntity.updatedAt).isToday();
        assertThat(savedUser.getEmail()).isEqualTo("test@email.com");
        assertThat(savedUser.getPassword()).isNull();
    }
}