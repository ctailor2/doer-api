package com.doerapispring.users;

import com.doerapispring.UserIdentifier;
import com.doerapispring.userSessions.OperationRefusedException;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
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
 * Created by chiragtailor on 8/18/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {
    private UserService userService;

    @Mock
    private NewUserRepository newUserRepository;

    @Captor
    private ArgumentCaptor<User> newUserArgumentCaptor;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        userService = new UserService(newUserRepository);
    }

    @Test
    public void create_whenIdentifierNotTaken_addsUserToRepository_returnsUser() throws Exception {
        UserIdentifier userIdentifier = new UserIdentifier("soUnique");

        when(newUserRepository.find(any())).thenReturn(Optional.empty());

        User createdUser = userService.create(userIdentifier);

        verify(newUserRepository).find(userIdentifier);
        verify(newUserRepository).add(newUserArgumentCaptor.capture());
        User addedUser = newUserArgumentCaptor.getValue();
        assertThat(addedUser.getIdentifier()).isEqualTo(userIdentifier);
        assertThat(createdUser).isNotNull();
    }

    @Test
    public void create_whenIdentifierTaken_refusesCreation() throws Exception {
        UserIdentifier userIdentifier = new UserIdentifier("soUnique");

        when(newUserRepository.find(any())).thenReturn(Optional.of(new User(userIdentifier)));

        exception.expect(OperationRefusedException.class);
        userService.create(userIdentifier);
    }
}