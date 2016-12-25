package com.doerapispring.domain;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.internal.verification.VerificationModeFactory;
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

        UserIdentifier userIdentifier = new UserIdentifier("one@two.com");
        List<Todo> todos = todoService.getByScheduling(userIdentifier, ScheduledFor.now);

        verify(masterListRepository).find(userIdentifier);
        assertThat(todos).isEmpty();
    }

    @Test
    public void getByScheduling_whenScheduledForNow_returnsImmediateTodos() throws Exception {
        UserIdentifier userIdentifier = new UserIdentifier("one@two.com");
        List<Todo> immediateTodos = Collections.singletonList(new Todo("one", ScheduledFor.anytime));
        MasterList masterList = new MasterList(userIdentifier, new ImmediateList(immediateTodos), new PostponedList(Collections.emptyList()));
        when(masterListRepository.find(any())).thenReturn(Optional.of(masterList));

        List<Todo> todos = todoService.getByScheduling(userIdentifier, ScheduledFor.now);

        verify(masterListRepository).find(userIdentifier);
        assertThat(todos).isEqualTo(immediateTodos);
    }

    @Test
    public void getByScheduling_whenScheduledForLater_returnsPostponedTodos() throws Exception {
        UserIdentifier userIdentifier = new UserIdentifier("one@two.com");
        List<Todo> postponedTodos = Collections.singletonList(new Todo("two", ScheduledFor.anytime));
        MasterList masterList = new MasterList(userIdentifier, new ImmediateList(Collections.emptyList()), new PostponedList(postponedTodos));
        when(masterListRepository.find(any())).thenReturn(Optional.of(masterList));

        List<Todo> todos = todoService.getByScheduling(userIdentifier, ScheduledFor.later);

        verify(masterListRepository).find(userIdentifier);
        assertThat(todos).isEqualTo(postponedTodos);
    }

    @Test
    public void getByScheduling_whenScheduledAnytime_returnsAllTodos() throws Exception {
        UserIdentifier userIdentifier = new UserIdentifier("one@two.com");
        List<Todo> immediateTodos = Collections.singletonList(new Todo("one", ScheduledFor.anytime));
        List<Todo> postponedTodos = Collections.singletonList(new Todo("two", ScheduledFor.anytime));
        MasterList masterList = new MasterList(userIdentifier, new ImmediateList(immediateTodos), new PostponedList(postponedTodos));
        when(masterListRepository.find(userIdentifier)).thenReturn(Optional.of(masterList));

        List<Todo> todos = todoService.getByScheduling(userIdentifier, ScheduledFor.anytime);

        verify(masterListRepository).find(userIdentifier);
        List<Todo> allTodos = new ArrayList<>(immediateTodos);
        allTodos.addAll(postponedTodos);
        assertThat(todos).isEqualTo(allTodos);
    }

    @Test
    public void create_whenMasterListNotFound_refusesCreate() throws Exception {
        when(masterListRepository.find(any())).thenReturn(Optional.empty());

        exception.expect(OperationRefusedException.class);
        UserIdentifier userIdentifier = new UserIdentifier("testItUp");
        todoService.create(userIdentifier, "some things", ScheduledFor.now);

        verify(masterListRepository.find(userIdentifier));
    }

    @Test
    public void create_whenMasterListFound_whenRepositoryRejectsModels_refusesCreate() throws Exception {
        UserIdentifier userIdentifier = new UserIdentifier("something");
        MasterList masterList = new MasterList(userIdentifier, null, null);
        when(masterListRepository.find(any())).thenReturn(Optional.of(masterList));
        doThrow(new AbnormalModelException()).when(masterListRepository).add(any(), any());

        exception.expect(OperationRefusedException.class);
        todoService.create(new UserIdentifier("testItUp"), "some things", ScheduledFor.now);

        verify(masterListRepository, VerificationModeFactory.atLeastOnce()).add(any(), any());
    }

    @Test
    public void create_whenMasterListFound_addsTodoToMasterList() throws Exception {
        UserIdentifier userIdentifier = new UserIdentifier("something");
        MasterList masterList = new MasterList(userIdentifier, null, null);
        when(masterListRepository.find(any())).thenReturn(Optional.of(masterList));

        todoService.create(new UserIdentifier("testItUp"), "some things", ScheduledFor.now);

        verify(masterListRepository).add(masterList, new Todo("some things", ScheduledFor.now));
    }
}