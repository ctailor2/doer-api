package com.doerapispring.users;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.Assert.*;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.verify;
import static org.fest.assertions.api.Assertions.assertThat;

/**
 * Created by chiragtailor on 8/18/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {
    private UserService userService;
    private UserEntity userEntity;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private UserRepository userRepository;

    private ArgumentCaptor<User> userArgumentCaptor = ArgumentCaptor.forClass(User.class);

    @Before
    public void setUp() throws Exception {
        userService = new UserService(userRepository, passwordEncoder);
        userEntity = UserEntity.builder()
                .email("test@email.com")
                .password("password")
                .passwordConfirmation("password")
                .build();
        doReturn("encodedPassword").when(passwordEncoder).encode(userEntity.getPassword());
    }

    @Test
    public void create_callsPasswordEncoder_callsUserRepository_setsFields() throws Exception {
        userService.create(userEntity);
        verify(userRepository).save(userArgumentCaptor.capture());
        User savedUser = userArgumentCaptor.getValue();
        assertThat(savedUser.email).isEqualTo("test@email.com");
        assertThat(savedUser.password_digest).isEqualTo("encodedPassword");
    }

    @Test
    public void create_setsAuditingData() throws Exception {
        userService.create(userEntity);
        verify(userRepository).save(userArgumentCaptor.capture());
        User savedUser = userArgumentCaptor.getValue();
        assertThat(savedUser.createdAt).isToday();
        assertThat(savedUser.updatedAt).isToday();
    }
}