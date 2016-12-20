package com.doerapispring.domain;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class TodoServiceTest {
    private TodoService todoService;

    @Mock
    private DomainRepository<Todo, String> todoRepository;

    @Mock
    private DomainRepository<MasterList, String> masterListRepository;

    @Captor
    private ArgumentCaptor<Todo> todoArgumentCaptor;
    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        todoService = new TodoService(todoRepository, masterListRepository);
    }

    @Test
    public void getByScheduling_whenScheduledForNow_returnsImmediateTodos() throws Exception {
        UserIdentifier userIdentifier = new UserIdentifier("one@two.com");
        List<Todo> immediateTodos = Collections.singletonList(new Todo(new UserIdentifier("one"), "one", ScheduledFor.anytime));
        MasterList masterList = new MasterList(new ImmediateList(immediateTodos), new PostponedList(Collections.emptyList()));
        when(masterListRepository.find(any())).thenReturn(Optional.of(masterList));

        List<Todo> todos = todoService.getByScheduling(userIdentifier, ScheduledFor.now);

        verify(masterListRepository).find(userIdentifier);
        assertThat(todos).isEqualTo(immediateTodos);
    }

    @Test
    public void getByScheduling_whenScheduledForLater_returnsPostponedTodos() throws Exception {
        UserIdentifier userIdentifier = new UserIdentifier("one@two.com");
        List<Todo> postponedTodos = Collections.singletonList(new Todo(new UserIdentifier("two"), "two", ScheduledFor.anytime));
        MasterList masterList = new MasterList(new ImmediateList(Collections.emptyList()), new PostponedList(postponedTodos));
        when(masterListRepository.find(any())).thenReturn(Optional.of(masterList));

        List<Todo> todos = todoService.getByScheduling(userIdentifier, ScheduledFor.later);

        verify(masterListRepository).find(userIdentifier);
        assertThat(todos).isEqualTo(postponedTodos);
    }

    @Test
    public void getByScheduling_whenScheduledAnytime_returnsAllTodos() throws Exception {
        UserIdentifier userIdentifier = new UserIdentifier("one@two.com");
        List<Todo> immediateTodos = Collections.singletonList(new Todo(new UserIdentifier("one"), "one", ScheduledFor.anytime));
        List<Todo> postponedTodos = Collections.singletonList(new Todo(new UserIdentifier("two"), "two", ScheduledFor.anytime));
        MasterList masterList = new MasterList(new ImmediateList(immediateTodos), new PostponedList(postponedTodos));
        when(masterListRepository.find(userIdentifier)).thenReturn(Optional.of(masterList));

        List<Todo> todos = todoService.getByScheduling(userIdentifier, ScheduledFor.anytime);

        verify(masterListRepository).find(userIdentifier);
        List<Todo> allTodos = new ArrayList<>(immediateTodos);
        allTodos.addAll(postponedTodos);
        assertThat(todos).isEqualTo(allTodos);
    }

    @Test
    public void create_addsTodoToRepository() throws Exception {
        UserIdentifier userIdentifier = new UserIdentifier("testItUp");
        String task = "some things";
        ScheduledFor scheduling = ScheduledFor.now;
        todoService.create(userIdentifier, task, scheduling);

        verify(todoRepository).add(todoArgumentCaptor.capture());
        Todo todo = todoArgumentCaptor.getValue();
        assertThat(todo).isNotNull();
        assertThat(todo.getUserIdentifier()).isEqualTo(userIdentifier);
        assertThat(todo.getTask()).isEqualTo(task);
        assertThat(todo.getScheduling()).isEqualTo(scheduling);
    }

    @Test
    public void create_whenRepositoryRejectsModel_refusesCreate() throws Exception {
        doThrow(new AbnormalModelException()).when(todoRepository).add(any());

        exception.expect(OperationRefusedException.class);
        todoService.create(new UserIdentifier("testItUp"), "some things", ScheduledFor.now);
    }
}