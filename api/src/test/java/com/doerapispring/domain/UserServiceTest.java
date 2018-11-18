package com.doerapispring.domain;

import com.doerapispring.web.InvalidRequestException;
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
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class UserServiceTest {
    private UserService userService;

    @Mock
    private ObjectRepository<User, UserId> userRepository;

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        userService = new UserService(userRepository);
    }

    @Test
    public void create_whenIdentifierNotTaken_addsUserToRepository_returnsUser() throws Exception {
        String identifier = "soUnique";

        when(userRepository.find(any(UserId.class))).thenReturn(Optional.empty());

        User createdUser = userService.create(identifier);

        UserId userId = new UserId(identifier);
        verify(userRepository).find(userId);
        verify(userRepository).save(userArgumentCaptor.capture());
        User addedUser = userArgumentCaptor.getValue();
        assertThat(addedUser.getUserId()).isEqualTo(userId);
        assertThat(createdUser).isNotNull();
    }

    @Test
    public void create_whenIdentifierTaken_refusesCreation() throws Exception {
        String identifier = "soUnique";

        when(userRepository.find(any(UserId.class))).thenReturn(Optional.of(new User(new UserId(identifier))));

        exception.expect(InvalidRequestException.class);
        userService.create(identifier);
    }

    @Test
    public void create_whenRepositoryRejectsModel_refusesCreation() throws Exception {
        when(userRepository.find(any(UserId.class))).thenReturn(Optional.empty());
        doThrow(new AbnormalModelException()).when(userRepository).save(any(User.class));

        exception.expect(InvalidRequestException.class);
        userService.create("soUnique");

    }
}