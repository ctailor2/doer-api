package com.doerapispring.domain;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MasterListTest {
    private MasterList masterList;

    private ArrayList<ListUnlock> listUnlocks;

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
        listUnlocks = new ArrayList<>();
        masterList = new MasterList(new UniqueIdentifier<>("something"), mockNowList, mockLaterList, listUnlocks);
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
    public void displace_whenTodoWithIdentifierExists_whenPostponedListIsEmpty_replacesTodo_andPushesItIntoPostponedListWithCorrectPositioning() throws Exception {
        Todo nowTodo = new Todo("someId", "someTask", ScheduledFor.now, 4);
        TodoList nowList = new TodoList(ScheduledFor.now, Collections.singletonList(nowTodo), 3);
        TodoList laterList = new TodoList(ScheduledFor.later, Collections.emptyList(), -1);
        MasterList masterList = new MasterList(new UniqueIdentifier<>("someIdentifier"), nowList, laterList, Collections.emptyList());

        List<Todo> todos = masterList.displace("someId", "displace it");

        // TODO: The local identifier behavior here seems weird. Why should the new todo
        // get the id of the original todo that was displaced and that one get a newly assigned identifier?
        Todo displacedTodo = new Todo("0", "someTask", ScheduledFor.later, 1);
        Todo newTodo = new Todo("someId", "displace it", ScheduledFor.now, 4);
        assertThat(todos).contains(displacedTodo, newTodo);
        assertThat(nowList.getTodos()).containsOnly(newTodo);
    }

    @Test
    public void displace_whenTodoWithIdentifierExists_whenPostponedIsNotEmpty_replacesTodo_andPushesItIntoPostponedListWithCorrectPositioning() throws Exception {
        Todo nowTodo = new Todo("someId", "someTask", ScheduledFor.now, 4);
        TodoList nowList = new TodoList(ScheduledFor.now, Collections.singletonList(nowTodo), 3);
        TodoList laterList = new TodoList(ScheduledFor.later, Collections.singletonList(new Todo("someOtherId", "someTask", ScheduledFor.later, 3)), -1);
        MasterList masterList = new MasterList(new UniqueIdentifier<>("someIdentifier"), nowList, laterList, Collections.emptyList());

        List<Todo> todos = masterList.displace("someId", "displace it");

        Todo displacedTodo = new Todo("0", "someTask", ScheduledFor.later, 2);
        Todo newTodo = new Todo("someId", "displace it", ScheduledFor.now, 4);
        assertThat(todos).contains(displacedTodo, newTodo);
        assertThat(nowList.getTodos()).containsOnly(newTodo);
    }

    @Test
    public void update_whenTodoWithIdentifierExists_updatesTodo() throws Exception {
        Todo todo = new Todo("someId", "someTask", ScheduledFor.now, 1);
        when(mockNowList.getTodos()).thenReturn(Collections.singletonList(todo));

        Todo updatedTodo = masterList.update(todo.getLocalIdentifier(), "someOtherTask");

        assertThat(updatedTodo).isEqualTo(new Todo(todo.getLocalIdentifier(), "someOtherTask", todo.getScheduling(), todo.getPosition()));
        assertThat(masterList.getAllTodos()).containsOnly(updatedTodo);
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
    public void recordView_whenThereAreNoListUnlocks_returnsAListUnlock() throws Exception {
        ListUnlock listUnlock = masterList.unlock();
        assertThat(listUnlock).isNotNull();
    }

    @Test
    public void recordView_whenThereAreListUnlocks_whenFirstListUnlockWasCreatedToday_throwsLockTimerNotExpiredException() throws Exception {
        listUnlocks.add(new ListUnlock());

        exception.expect(LockTimerNotExpiredException.class);
        masterList.unlock();
    }

    @Test
    public void recordView_whenThereAreListUnlocks_whenFirstListUnlockWasCreatedBeforeToday_returnsAListUnlock() throws Exception {
        listUnlocks.add(new ListUnlock(new Date(0L)));

        ListUnlock listUnlock = masterList.unlock();
        assertThat(listUnlock).isNotNull();
    }

    @Test
    public void isLocked_whenThereAreNoListUnlocks_returnsTrue() throws Exception {
        assertThat(masterList.isLocked()).isTrue();
    }

    @Test
    public void isLocked_whenThereAreListUnlocks_whenItHasBeenMoreThan30MinutesSinceFirstUnlockCreated_returnsTrue() throws Exception {
        Instant now = Instant.now();
        Date lastUnlockDate = new Date(now.minusSeconds(1801L).toEpochMilli());
        listUnlocks.add(new ListUnlock(lastUnlockDate));

        assertThat(masterList.isLocked()).isTrue();
    }

    @Test
    public void isLocked_whenThereAreListUnlocks_whenItHasBeen30MinutesOrLessSinceFirstUnlockCreated_returnsFalse() throws Exception {
        Instant now = Instant.now();
        Date lastUnlockDate = new Date(now.minusSeconds(1799L).toEpochMilli());
        listUnlocks.add(new ListUnlock(lastUnlockDate));

        assertThat(masterList.isLocked()).isFalse();
    }

    @Test
    public void pull_whenThereAreNoImmediateTodos_fillsFromPostponedList() throws Exception {
        List<Todo> todos = asList(
            new Todo("A", "firstLater", ScheduledFor.later, 1),
            new Todo("B", "secondLater", ScheduledFor.later, 2));
        TodoList nowList = new TodoList(ScheduledFor.now, Collections.emptyList(), 2);
        TodoList laterList = new TodoList(ScheduledFor.later, todos, -1);
        MasterList masterList = new MasterList(new UniqueIdentifier<>("something"), nowList, laterList, Collections.emptyList());

        List<Todo> effectedTodos = masterList.pull();

        assertThat(effectedTodos).containsOnly(
            new Todo("A", "firstLater", ScheduledFor.now, 1),
            new Todo("B", "secondLater", ScheduledFor.now, 2));
        assertThat(masterList.getTodos()).isEqualTo(effectedTodos);
    }

    @Test
    public void pull_whenThereAreLessImmediateTodosThanMaxSize_fillsFromPostponedList_withAsManyTodosAsTheDifference() throws Exception {
        TodoList nowList = new TodoList(
            ScheduledFor.now,
            Collections.singletonList(new Todo("A", "firstNow", ScheduledFor.now, 1)),
            3);
        TodoList laterList = new TodoList(
            ScheduledFor.later,
            asList(
                new Todo("B", "firstLater", ScheduledFor.later, 1),
                new Todo("C", "secondLater", ScheduledFor.later, 2),
                new Todo("D", "thirdLater", ScheduledFor.later, 3)),
            -1);
        MasterList masterList = new MasterList(new UniqueIdentifier<>("something"), nowList, laterList, Collections.emptyList());

        List<Todo> effectedTodos = masterList.pull();

        assertThat(effectedTodos).contains(new Todo("B", "firstLater", ScheduledFor.now, 2));
        assertThat(masterList.getTodos()).containsExactly(
            new Todo("A", "firstNow", ScheduledFor.now, 1),
            new Todo("B", "firstLater", ScheduledFor.now, 2),
            new Todo("C", "secondLater", ScheduledFor.now, 3));
    }

    @Test
    public void pull_whenThereAreAsManyImmediateTodosAsMaxSize_doesNotFillFromPostponedList() throws Exception {
        Todo todo = new Todo("B", "firstLater", ScheduledFor.later, 1);
        TodoList laterList = new TodoList(ScheduledFor.later, Collections.singletonList(todo), -1);
        TodoList nowList = new TodoList(ScheduledFor.now, Collections.emptyList(), 0);
        MasterList masterList = new MasterList(new UniqueIdentifier<>("something"), nowList, laterList, Collections.emptyList());

        List<Todo> todos = masterList.pull();

        assertThat(todos).isEmpty();
        assertThat(nowList.getTodos()).isEmpty();
    }

    @Test
    public void pull_whenThereIsASourceList_whenThereAreLessTodosThanMaxSize_whenSourceListIsEmpty_doesNotFillListFromSource() throws Exception {
        Todo nowTodo = new Todo("A", "onlyNow", ScheduledFor.now, 1);
        TodoList laterList = new TodoList(ScheduledFor.later, Collections.emptyList(), -1);
        TodoList nowList = new TodoList(ScheduledFor.now, Collections.singletonList(nowTodo), 3);
        MasterList masterList = new MasterList(new UniqueIdentifier<>("something"), nowList, laterList, Collections.emptyList());

        List<Todo> todos = masterList.pull();

        assertThat(todos).isEmpty();
        assertThat(nowList.getTodos()).containsOnly(nowTodo);
    }

    @Test
    public void getTodos_getsTodosFromImmediateList() {
        masterList.getTodos();

        verify(mockNowList).getTodos();
    }

    @Test
    public void getDeferredTodos_whenListIsLocked_throwsListTimerNotExpiredException() throws Exception {
        exception.expect(LockTimerNotExpiredException.class);
        masterList.getDeferredTodos();
    }

    @Test
    public void getDeferredTodos_whenListIsNotLocked_getsTodosFromPostponedList() throws Exception {
        Instant now = Instant.now();
        Date lastUnlockDate = new Date(now.minusSeconds(1799L).toEpochMilli());
        listUnlocks.add(new ListUnlock(lastUnlockDate));
        masterList.getDeferredTodos();

        verify(mockLaterList).getTodos();
    }

    @Test
    public void isFull() {
        when(mockNowList.isFull()).thenReturn(true);

        assertThat(masterList.isFull()).isTrue();
    }

    @Test
    public void isAbleToBeReplenished_whenThereAreDeferredTodos_andTheListIsNotFull_returnsTrue() throws Exception {
        when(mockNowList.isFull()).thenReturn(false);
        when(mockLaterList.getTodos()).thenReturn(Collections.singletonList(new Todo("someTask", ScheduledFor.later, 1)));

        boolean hasDeferredTodosAvailable = masterList.isAbleToBeReplenished();

        assertThat(hasDeferredTodosAvailable).isTrue();
    }

    @Test
    public void isAbleToBeReplenished_whenThereAreDeferredTodos_andTheListIsFull_returnsFalse() {
        when(mockNowList.isFull()).thenReturn(true);
        when(mockLaterList.getTodos()).thenReturn(Collections.singletonList(new Todo("someTask", ScheduledFor.later, 1)));

        boolean hasDeferredTodosAvailable = masterList.isAbleToBeReplenished();

        assertThat(hasDeferredTodosAvailable).isFalse();
    }
}