package com.doerapispring.domain;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
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
    private AggregateRootRepository<MasterList, Todo, String> masterListRepository;

    @Rule
    public final ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        todoService = new TodoService(masterListRepository);
    }

    @Test
    public void getByScheduling_whenThereAreNoTodos_returnsAnEmptyList() throws Exception {
        when(masterListRepository.find(any())).thenReturn(Optional.empty());

        UniqueIdentifier uniqueIdentifier = new UniqueIdentifier("one@two.com");
        User user = new User(uniqueIdentifier);
        List<Todo> todos = todoService.getByScheduling(user, ScheduledFor.now);

        verify(masterListRepository).find(uniqueIdentifier);
        assertThat(todos).isEmpty();
    }

    @Test
    public void getByScheduling_whenScheduledForNow_returnsImmediateTodos() throws Exception {
        UniqueIdentifier uniqueIdentifier = new UniqueIdentifier("one@two.com");
        User user = new User(uniqueIdentifier);
        List<Todo> immediateTodos = Collections.singletonList(new Todo("someId", "one", ScheduledFor.anytime));
        MasterList masterList = new MasterList(uniqueIdentifier, new ImmediateList(immediateTodos), new PostponedList(Collections.emptyList()));
        when(masterListRepository.find(any())).thenReturn(Optional.of(masterList));

        List<Todo> todos = todoService.getByScheduling(user, ScheduledFor.now);

        verify(masterListRepository).find(uniqueIdentifier);
        assertThat(todos).isEqualTo(immediateTodos);
    }

    @Test
    public void getByScheduling_whenScheduledForLater_returnsPostponedTodos() throws Exception {
        UniqueIdentifier uniqueIdentifier = new UniqueIdentifier("one@two.com");
        User user = new User(uniqueIdentifier);
        List<Todo> postponedTodos = Collections.singletonList(new Todo("someId", "two", ScheduledFor.anytime));
        MasterList masterList = new MasterList(uniqueIdentifier, new ImmediateList(Collections.emptyList()), new PostponedList(postponedTodos));
        when(masterListRepository.find(any())).thenReturn(Optional.of(masterList));

        List<Todo> todos = todoService.getByScheduling(user, ScheduledFor.later);

        verify(masterListRepository).find(uniqueIdentifier);
        assertThat(todos).isEqualTo(postponedTodos);
    }

    @Test
    public void getByScheduling_whenScheduledAnytime_returnsAllTodos() throws Exception {
        UniqueIdentifier uniqueIdentifier = new UniqueIdentifier("one@two.com");
        User user = new User(uniqueIdentifier);
        List<Todo> immediateTodos = Collections.singletonList(new Todo("someId", "one", ScheduledFor.anytime));
        List<Todo> postponedTodos = Collections.singletonList(new Todo("someId", "two", ScheduledFor.anytime));
        MasterList masterList = new MasterList(uniqueIdentifier, new ImmediateList(immediateTodos), new PostponedList(postponedTodos));
        when(masterListRepository.find(uniqueIdentifier)).thenReturn(Optional.of(masterList));

        List<Todo> todos = todoService.getByScheduling(user, ScheduledFor.anytime);

        verify(masterListRepository).find(uniqueIdentifier);
        List<Todo> allTodos = new ArrayList<>(immediateTodos);
        allTodos.addAll(postponedTodos);
        assertThat(todos).isEqualTo(allTodos);
    }

    @Test
    public void create_whenMasterListNotFound_refusesCreate() throws Exception {
        when(masterListRepository.find(any())).thenReturn(Optional.empty());

        exception.expect(OperationRefusedException.class);
        UniqueIdentifier uniqueIdentifier = new UniqueIdentifier("testItUp");
        User user = new User(uniqueIdentifier);
        todoService.create(user, "some things", ScheduledFor.now);

        verify(masterListRepository.find(uniqueIdentifier));
    }

    @Test
    public void create_whenMasterListFound_addsTodoToRepository() throws Exception {
        MasterList mockMasterList = mock(MasterList.class);
        when(masterListRepository.find(any())).thenReturn(Optional.of(mockMasterList));
        Todo todo = new Todo("someId", "some things", ScheduledFor.now);
        when(mockMasterList.add(any(), any())).thenReturn(todo);

        todoService.create(new User(new UniqueIdentifier("testItUp")), "some things", ScheduledFor.now);

        verify(mockMasterList).add("some things", ScheduledFor.now);
        verify(masterListRepository).add(mockMasterList, todo);
    }

    @Test
    public void create_whenMasterListFound_whenRepositoryRejectsModels_refusesCreate() throws Exception {
        MasterList masterList = mock(MasterList.class);
        when(masterListRepository.find(any())).thenReturn(Optional.of(masterList));
        doThrow(new AbnormalModelException()).when(masterListRepository).add(any(), any());

        exception.expect(OperationRefusedException.class);
        todoService.create(new User(new UniqueIdentifier("testItUp")), "some things", ScheduledFor.now);
    }
}