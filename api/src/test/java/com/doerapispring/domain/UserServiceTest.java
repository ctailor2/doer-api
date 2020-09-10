package com.doerapispring.domain;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import scala.jdk.javaapi.CollectionConverters;

import java.time.Clock;
import java.time.Instant;
import java.util.Collections;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@RunWith(org.mockito.junit.MockitoJUnitRunner.class)
public class UserServiceTest {
    private UserService userService;

    @Mock
    private ObjectRepository<User, UserId> userRepository;

    @Mock
    private OwnedObjectRepository<TodoList, UserId, ListId> todoListRepository;

    @Mock
    private OwnedObjectWriteRepository<Snapshot<TodoListModel>, UserId, ListId> todoListModelSnapshotRepository;

    private TodoListFactory todoListFactory = mock(TodoListFactory.class);

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    private Clock clock = mock(Clock.class);

    private TodoList newTodoList;

    @Before
    public void setUp() throws Exception {
        userService = new UserService(userRepository, todoListFactory, todoListRepository, todoListModelSnapshotRepository, clock);

        when(userRepository.find(any(UserId.class))).thenReturn(Optional.empty());
        newTodoList = mock(TodoList.class);
        when(todoListFactory.todoList(any(), any(), any())).thenReturn(newTodoList);
        when(clock.instant()).thenReturn(Instant.now());
    }

    @Test
    public void create_whenIdentifierNotTaken_createsDefaultTodoListForUser_andSavesIt() {
        ListId listId = new ListId("someListId");
        when(todoListRepository.nextIdentifier()).thenReturn(listId);
        String identifier = "soUnique";
        Instant instant = Instant.ofEpochSecond(15324234L);
        when(clock.instant()).thenReturn(instant);

        userService.create(identifier);

        verify(todoListFactory).todoList(new UserId(identifier), listId, "default");
        verify(todoListRepository).save(newTodoList);
    }

    @Test
    public void create_createsTodoListModelSnapshot() {
        String identifier = "soUnique";
        Instant instant = Instant.ofEpochSecond(15324234L);
        when(clock.instant()).thenReturn(instant);
        TodoList todoList = new TodoList(new UserId(identifier), new ListId("someListId"), "someName");
        when(todoListFactory.todoList(any(), any(), any())).thenReturn(todoList);

        userService.create(identifier);

        java.util.List<Todo> todos = Collections.emptyList();
        java.util.List<CompletedTodo> completedTodos = Collections.emptyList();
        verify(todoListModelSnapshotRepository).save(
                new UserId(identifier),
                todoList.getListId(),
                new Snapshot<>(
                        new TodoListModel(
                                CollectionConverters.asScala(todos).toList(),
                                CollectionConverters.asScala(completedTodos).toList(),
                                new Date(0L),
                                0),
                        Date.from(instant)));
    }

    @Test
    public void create_whenIdentifierNotTaken_addsUserToRepository() {
        ListId listId = new ListId("someListId");
        when(todoListRepository.nextIdentifier()).thenReturn(listId);
        String identifier = "soUnique";
        UserId userId = new UserId(identifier);
        when(todoListFactory.todoList(userId, listId, "default")).thenReturn(new TodoList(userId, listId, null));

        User returnedUser = userService.create(identifier);

        verify(userRepository).find(userId);
        verify(userRepository).save(userArgumentCaptor.capture());
        User addedUser = userArgumentCaptor.getValue();
        assertThat(addedUser.getUserId()).isEqualTo(userId);
        assertThat(addedUser.getDefaultListId()).isEqualTo(listId);
        assertThat(returnedUser).isNotNull();
    }

    @Test
    public void create_whenIdentifierTaken_refusesCreation_doesNotCreateDefaultTodoList() {
        String identifier = "soUnique";

        when(userRepository.find(any(UserId.class))).thenReturn(Optional.of(new User(new UserId(identifier), new ListId("someListId"))));

        assertThatThrownBy(() -> userService.create(identifier))
            .isInstanceOf(UserAlreadyExistsException.class);
        verifyZeroInteractions(todoListFactory);
    }
}