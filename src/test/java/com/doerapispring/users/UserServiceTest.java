package com.doerapispring.users;

import com.doerapispring.UserIdentifier;
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
import static org.mockito.Mockito.*;

/**
 * Created by chiragtailor on 8/18/16.
 */
@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {
    private UserService userService;

    @Mock
    private NewUserRepository newUserRepository;

    @Captor
    private ArgumentCaptor<NewUser> newUserArgumentCaptor;

    @Before
    public void setUp() throws Exception {
        userService = new UserService(newUserRepository);
    }

    @Test
    public void create_whenIdentifierNotTaken_addsUserToRepository_returnsUser() throws Exception {
        UserIdentifier userIdentifier = new UserIdentifier("soUnique");

        when(newUserRepository.find(any())).thenReturn(Optional.empty());

        NewUser createdUser = userService.create(userIdentifier);

        verify(newUserRepository).find(userIdentifier);
        verify(newUserRepository).add(newUserArgumentCaptor.capture());
        NewUser addedUser = newUserArgumentCaptor.getValue();
        assertThat(addedUser.getIdentifier()).isEqualTo(userIdentifier);
        assertThat(createdUser).isNotNull();
    }

    @Test
    public void create_whenIdentifierTaken_doesNotAddUserToRepository_returnsNull() throws Exception {
        UserIdentifier userIdentifier = new UserIdentifier("soUnique");

        when(newUserRepository.find(any())).thenReturn(Optional.of(new NewUser(userIdentifier)));

        NewUser createdUser = userService.create(userIdentifier);

        verify(newUserRepository).find(userIdentifier);
        verifyNoMoreInteractions(newUserRepository);
        assertThat(createdUser).isNull();
    }
}