package com.doerapispring.domain;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MasterListTest {
    private MasterList masterList;

    @Mock
    private TodoList mockNowList;

    @Mock
    private TodoList mockLaterList;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        when(mockNowList.getTodos()).thenReturn(Collections.emptyList());
        when(mockLaterList.getTodos()).thenReturn(Collections.emptyList());
        masterList = new MasterList(new UniqueIdentifier<>("something"), mockNowList, mockLaterList);
    }

    @Test
    public void add_whenScheduledForNow_addsToNowList() throws ListSizeExceededException, DuplicateTodoException {
        masterList.add("someTask", ScheduledFor.now);

        verify(mockNowList).add("someTask");
    }

    @Test
    public void add_whenScheduledForLater_addsToLaterList() throws ListSizeExceededException, DuplicateTodoException {
        masterList.add("someTask", ScheduledFor.later);

        verify(mockLaterList).add("someTask");
    }

    @Test
    public void add_whenListAlreadyContainsTask_doesNotAdd_throwsDuplicateTodoException() throws Exception {
        Todo todo = new Todo("someId", "sameTask", ScheduledFor.now, 1);
        when(mockNowList.getTodos()).thenReturn(Collections.singletonList(todo));

        exception.expect(DuplicateTodoException.class);
        masterList.add("sameTask", ScheduledFor.now);
    }

    @Test
    public void delete_whenTodoWithIdentifierExists_removesTodoFromMatchingList() throws Exception {
        Todo todo = new Todo("someId", "someTask", ScheduledFor.now, 1);
        when(mockNowList.getTodos()).thenReturn(Collections.singletonList(todo));

        Todo deletedTodo = masterList.delete(todo.getLocalIdentifier());

        assertThat(deletedTodo).isEqualTo(todo);
        verify(mockNowList).remove(todo);
    }

    @Test
    public void delete_whenTodoWithIdentifierDoesNotExist_throwsNotFoundException() throws Exception {
        exception.expect(TodoNotFoundException.class);
        masterList.delete("someBogusIdentifier");
    }

    @Test
    public void displace_whenTodoWithIdentifierDoesNotExist_throwsNotFoundException() throws Exception {
        exception.expect(TodoNotFoundException.class);
        masterList.displace("someId", "displace it");
    }

    @Test
    public void displace_whenTaskAlreadyExists_throwsDuplicateTodoException() throws Exception {
        Todo todo = new Todo("someId", "sameTask", ScheduledFor.now, 1);
        when(mockNowList.getTodos()).thenReturn(Collections.singletonList(todo));

        exception.expect(DuplicateTodoException.class);
        masterList.displace(todo.getLocalIdentifier(), "sameTask");
    }

    @Test
    public void displace_whenTodoWithIdentifierExists_displacesTodoInMatchingList() throws TodoNotFoundException, DuplicateTodoException, NoSourceListConfiguredException {
        Todo originalTodo = new Todo("someId", "someTask", ScheduledFor.now, 1);
        when(mockNowList.getTodos()).thenReturn(Collections.singletonList(originalTodo));

        masterList.displace(originalTodo.getLocalIdentifier(), "coolNewTask");

        verify(mockNowList).displace(originalTodo, "coolNewTask");
    }

    @Test
    public void update_whenTodoWithIdentifierExists_updatesTodo() throws Exception {
        Todo todo = new Todo("someId", "someTask", ScheduledFor.now, 1);
        when(mockNowList.getTodos()).thenReturn(Collections.singletonList(todo));

        Todo updatedTodo = masterList.update(todo.getLocalIdentifier(), "someOtherTask");

        assertThat(updatedTodo).isEqualTo(new Todo(todo.getLocalIdentifier(), "someOtherTask", todo.getScheduling(), todo.getPosition()));
        assertThat(masterList.getTodos()).containsOnly(updatedTodo);
    }

    @Test
    public void update_whenTaskAlreadyExists_throwsDuplicateTodoException() throws Exception {
        Todo todo = new Todo("someId", "sameTask", ScheduledFor.now, 1);
        when(mockNowList.getTodos()).thenReturn(Collections.singletonList(todo));

        exception.expect(DuplicateTodoException.class);
        masterList.update(todo.getLocalIdentifier(), "sameTask");
    }

    @Test
    public void update_whenTodoWithIdentifierDoesNotExist_throwsNotFoundException() throws Exception {
        exception.expect(TodoNotFoundException.class);
        masterList.update("bananaPudding", "sameTask");
    }

    @Test
    public void complete_whenTodoWithIdentifierExists_removesTodoFromMatchingList_returnsCompletedTodo() throws Exception {
        Todo todo = new Todo("someId", "someTask", ScheduledFor.now, 1);
        when(mockNowList.getTodos()).thenReturn(Collections.singletonList(todo));

        Todo completedTodo = masterList.complete(todo.getLocalIdentifier());

        verify(mockNowList).remove(todo);
        assertThat(completedTodo.isComplete()).isEqualTo(true);
    }

    @Test
    public void complete_whenTodoWithIdentifierDoesNotExist_throwsNotFoundException() throws Exception {
        exception.expect(TodoNotFoundException.class);
        masterList.complete("someBogusIdentifier");
    }

    @Test
    public void move_whenTodoWithIdentifierDoesNotExist_throwsNotFoundException() throws Exception {
        exception.expect(TodoNotFoundException.class);
        masterList.move("junk", "bogus");
    }

    @Test
    public void move_whenTodoWithIdentifierExists_findsTargetInMatchingList() throws TodoNotFoundException, ListSizeExceededException, DuplicateTodoException {
        Todo todo = new Todo("someId", "someTask", ScheduledFor.now, 1);
        when(mockNowList.getTodos()).thenReturn(Collections.singletonList(todo));

        masterList.move(todo.getLocalIdentifier(), "someOtherId");

        verify(mockNowList).getByIdentifier("someOtherId");
    }

    @Test
    public void move_whenTodoWithIdentifierExists_whenTargetNotFoundInList_throwsTodoNotFoundException() throws Exception {
        Todo todo = new Todo("someId", "someTask", ScheduledFor.now, 1);
        when(mockNowList.getTodos()).thenReturn(Collections.singletonList(todo));
        when(mockNowList.getByIdentifier(any())).thenThrow(new TodoNotFoundException());

        exception.expect(TodoNotFoundException.class);
        masterList.move(todo.getLocalIdentifier(), "nonExistentIdentifier");
    }

    @Test
    public void move_whenTodoWithIdentifierExists_whenTargetFoundInList_movesTodoInMatchingList() throws TodoNotFoundException {
        Todo originalTodo = new Todo("someId", "someTask", ScheduledFor.now, 1);
        when(mockNowList.getTodos()).thenReturn(Collections.singletonList(originalTodo));
        Todo targetTodo = new Todo("someOtherId", "someTask", ScheduledFor.now, 2);
        when(mockNowList.getByIdentifier(any())).thenReturn(targetTodo);

        masterList.move(originalTodo.getLocalIdentifier(), "someOtherId");

        verify(mockNowList).move(originalTodo, targetTodo);
    }

    @Test
    public void pull_pullsFromNowList_returnsResults() throws NoSourceListConfiguredException {
        List<Todo> todos = Collections.singletonList(new Todo("someId", "someTask", ScheduledFor.now, 1));
        when(mockNowList.pull()).thenReturn(todos);

        List<Todo> effectedTodos = masterList.pull();

        verify(mockNowList).pull();
        assertThat(effectedTodos).isEqualTo(todos);
    }
}