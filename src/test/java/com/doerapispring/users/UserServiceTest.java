package com.doerapispring.users;

import com.doerapispring.utilities.PasswordEncodingService;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

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

    @Mock
    private NewUserRepository newUserRepository;

    @Mock
    private PasswordEncodingService passwordEncodingService;

    private ArgumentCaptor<UserEntity> userEntityArgumentCaptor = ArgumentCaptor.forClass(UserEntity.class);

    private ArgumentCaptor<RegisteredUser> registeredUserArgumentCaptor = ArgumentCaptor.forClass(RegisteredUser.class);

    @Before
    public void setUp() throws Exception {
        userService = new UserService(userRepository, newUserRepository, passwordEncodingService, passwordEncoder);
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
        verify(userRepository).save(userEntityArgumentCaptor.capture());
        UserEntity savedUserEntity = userEntityArgumentCaptor.getValue();
        assertThat(savedUserEntity.email).isEqualTo("test@email.com");
        assertThat(savedUserEntity.passwordDigest).isEqualTo("encodedPassword");
        assertThat(savedUserEntity.createdAt).isToday();
        assertThat(savedUserEntity.updatedAt).isToday();
        assertThat(savedUser.getEmail()).isEqualTo("test@email.com");
        assertThat(savedUser.getPassword()).isNull();
    }

    @Test
    public void createRegisteredUser_callsPasswordEncodingService_createsRegisteredUser_addsToUserRepository() throws Exception {
        when(passwordEncodingService.encode(any())).thenReturn("encodedPassword");

        userService.createRegisteredUser("test@email.com", "password");

        verify(passwordEncodingService).encode("password");
        verify(newUserRepository).add(registeredUserArgumentCaptor.capture());
        RegisteredUser user = registeredUserArgumentCaptor.getValue();
        assertThat(user).isNotNull();
        assertThat(user.getEmail()).isEqualTo("test@email.com");
        assertThat(user.getEncodedPassword()).isEqualTo("encodedPassword");
    }
}