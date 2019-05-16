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

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.*;

@RunWith(org.mockito.junit.MockitoJUnitRunner.class)
public class UserServiceTest {
    private UserService userService;

    @Mock
    private ObjectRepository<User, UserId> userRepository;

    @Mock
    private OwnedObjectRepository<TodoList, UserId, ListId> todoListRepository;

    private TodoListFactory todoListFactory = mock(TodoListFactory.class);

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        userService = new UserService(userRepository, todoListFactory, todoListRepository);

        when(userRepository.find(any(UserId.class))).thenReturn(Optional.empty());
    }

    @Test
    public void create_whenIdentifierNotTaken_addsUserToRepository_returnsUser() throws Exception {
        String identifier = "soUnique";

        User createdUser = userService.create(identifier);

        UserId userId = new UserId(identifier);
        verify(userRepository).find(userId);
        verify(userRepository).save(userArgumentCaptor.capture());
        User addedUser = userArgumentCaptor.getValue();
        assertThat(addedUser.getUserId()).isEqualTo(userId);
        assertThat(createdUser).isNotNull();
    }

    @Test
    public void create_whenIdentifierNotTaken_createsDefaultTodoListForUser_andSavesIt() throws Exception {
        TodoList newTodoList = mock(TodoList.class);
        ListId listId = new ListId("someListId");
        when(todoListRepository.nextIdentifier()).thenReturn(listId);
        when(todoListFactory.todoList(any(), any(), any())).thenReturn(newTodoList);
        String identifier = "soUnique";

        userService.create(identifier);

        verify(todoListFactory).todoList(new UserId(identifier), listId, "default");
        verify(todoListRepository).save(newTodoList);
    }

    @Test
    public void create_whenIdentifierTaken_refusesCreation_doesNotCreateDefaultTodoList() throws Exception {
        String identifier = "soUnique";

        when(userRepository.find(any(UserId.class))).thenReturn(Optional.of(new User(new UserId(identifier))));

        assertThatThrownBy(() -> userService.create(identifier))
            .isInstanceOf(InvalidRequestException.class);
        verifyZeroInteractions(todoListFactory);
    }
}