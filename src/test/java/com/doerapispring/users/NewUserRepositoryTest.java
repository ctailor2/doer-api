package com.doerapispring.users;

import com.doerapispring.Identifier;
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

    @Test
    public void add_newUser_callsUserDao_savesFields_setsAuditingData_addsEmptyPassword_returnsUser() throws Exception {
        NewUser newUser = new NewUser(new Identifier("soUnique"));

        newUserRepository.add(newUser);

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

        Optional<NewUser> userOptional = newUserRepository.find(new Identifier("soUnique"));

        verify(userDAO).findByEmail("soUnique");
        assertThat(userOptional.isPresent()).isEqualTo(true);
    }

    @Test
    public void find_callsUserDao_whenUserNotFound_returnsEmptyOptional() throws Exception {
        when(userDAO.findByEmail(any())).thenReturn(null);

        Optional<NewUser> userOptional = newUserRepository.find(new Identifier("soUnique"));

        verify(userDAO).findByEmail("soUnique");
        assertThat(userOptional.isPresent()).isEqualTo(false);

    }
}