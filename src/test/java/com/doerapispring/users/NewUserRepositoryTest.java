package com.doerapispring.users;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

/**
 * Created by chiragtailor on 10/26/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class NewUserRepositoryTest {
    private NewUserRepository newUserRepository;

    @Mock
    private UserDAO userDAO;

    @Captor
    ArgumentCaptor<UserEntity> userEntityArgumentCaptor;

    @Before
    public void setUp() throws Exception {
        newUserRepository = new NewUserRepository(userDAO);
    }

    @Test
    public void add_registeredUser_callsUserDao_savesFields_setsAuditingData() throws Exception {
        RegisteredUser registeredUser = new RegisteredUser("test@email.com", "soEncodedPassword");

        newUserRepository.add(registeredUser);

        verify(userDAO).save(userEntityArgumentCaptor.capture());
        UserEntity userEntity = userEntityArgumentCaptor.getValue();
        assertThat(userEntity.email).isEqualTo("test@email.com");
        assertThat(userEntity.passwordDigest).isEqualTo("soEncodedPassword");
        assertThat(userEntity.createdAt).isToday();
        assertThat(userEntity.updatedAt).isToday();
    }
}