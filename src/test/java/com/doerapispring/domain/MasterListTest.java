package com.doerapispring.domain;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@RunWith(MockitoJUnitRunner.class)
public class MasterListTest {
    private MasterList masterList;

    @Mock
    private TodoList mockNowList;

    @Mock
    private TodoList mockLaterList;

    @Mock
    private Clock mockClock;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        when(mockClock.getZone()).thenReturn(ZoneId.of("UTC"));
        when(mockNowList.getTodos()).thenReturn(emptyList());
        when(mockLaterList.getTodos()).thenReturn(emptyList());
        masterList = new MasterList(mockClock, new UniqueIdentifier<>("something"), mockNowList, mockLaterList, new ArrayList<>());
        // TODO: The #displace and #pull tests are using MasterLists with real TodoList
        // and testing the behavior functionally. Try using the mocks and testing in isolation.
        // If this is difficult, maybe the todo list class behavior should be merged with MasterList.
    }

    @Test
    public void add_addsToNowList() throws ListSizeExceededException, DuplicateTodoException {
        masterList.add("someTask");

        verify(mockNowList).add("someTask");
    }

    @Test
    public void add_whenListAlreadyContainsTask_doesNotAdd_throwsDuplicateTodoException() throws Exception {
        Todo todo = new Todo("someId", "sameTask", MasterList.NAME, 1);
        when(mockNowList.getTodos()).thenReturn(singletonList(todo));

        exception.expect(DuplicateTodoException.class);
        masterList.add("sameTask");
    }

    @Test
    public void addDeferred_addsToLaterList() throws ListSizeExceededException, DuplicateTodoException {
        masterList.addDeferred("someTask");

        verify(mockLaterList).add("someTask");
    }

    @Test
    public void addDeferred_whenListAlreadyContainsTask_doesNotAdd_throwsDuplicateTodoException() throws Exception {
        Todo todo = new Todo("someId", "sameTask", MasterList.NAME, 1);
        when(mockNowList.getTodos()).thenReturn(singletonList(todo));

        exception.expect(DuplicateTodoException.class);
        masterList.addDeferred("sameTask");
    }

    @Test
    public void delete_whenTodoWithIdentifierExists_removesTodoFromMatchingList() throws Exception {
        Todo todo = new Todo("someId", "someTask", MasterList.NAME, 1);
        when(mockNowList.getTodos()).thenReturn(singletonList(todo));

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
        Todo todo = new Todo("someId", "sameTask", MasterList.NAME, 1);
        when(mockNowList.getTodos()).thenReturn(singletonList(todo));

        exception.expect(DuplicateTodoException.class);
        masterList.displace(todo.getLocalIdentifier(), "sameTask");
    }

    @Test
    public void displace_whenTodoWithIdentifierExists_whenPostponedListIsEmpty_replacesTodo_andPushesItIntoPostponedListWithCorrectPositioning() throws Exception {
        Todo nowTodo = new Todo("someId", "someTask", MasterList.NAME, 4);
        TodoList nowList = new TodoList(MasterList.NAME, Collections.singletonList(nowTodo), 3);
        TodoList laterList = new TodoList(MasterList.DEFERRED_NAME, Collections.emptyList(), -1);
        MasterList masterList = new MasterList(mockClock, new UniqueIdentifier<>("someIdentifier"), nowList, laterList, Collections.emptyList());

        List<Todo> todos = masterList.displace("someId", "displace it");

        // TODO: The local identifier behavior here seems weird. Why should the new todo
        // get the id of the original todo that was displaced and that one get a newly assigned identifier?
        Todo displacedTodo = new Todo("0", "someTask", MasterList.DEFERRED_NAME, 1);
        Todo newTodo = new Todo("someId", "displace it", MasterList.NAME, 4);
        assertThat(todos).contains(displacedTodo, newTodo);
        assertThat(nowList.getTodos()).containsOnly(newTodo);
    }

    @Test
    public void displace_whenTodoWithIdentifierExists_whenPostponedIsNotEmpty_replacesTodo_andPushesItIntoPostponedListWithCorrectPositioning() throws Exception {
        Todo nowTodo = new Todo("someId", "someTask", MasterList.NAME, 4);
        TodoList nowList = new TodoList(MasterList.NAME, Collections.singletonList(nowTodo), 3);
        TodoList laterList = new TodoList(MasterList.DEFERRED_NAME, Collections.singletonList(new Todo("someOtherId", "someTask", MasterList.DEFERRED_NAME, 3)), -1);
        MasterList masterList = new MasterList(mockClock, new UniqueIdentifier<>("someIdentifier"), nowList, laterList, Collections.emptyList());

        List<Todo> todos = masterList.displace("someId", "displace it");

        Todo displacedTodo = new Todo("0", "someTask", MasterList.DEFERRED_NAME, 2);
        Todo newTodo = new Todo("someId", "displace it", MasterList.NAME, 4);
        assertThat(todos).contains(displacedTodo, newTodo);
        assertThat(nowList.getTodos()).containsOnly(newTodo);
    }

    @Test
    public void update_whenTodoWithIdentifierExists_updatesTodo() throws Exception {
        Todo todo = new Todo("someId", "someTask", MasterList.NAME, 1);
        when(mockNowList.getTodos()).thenReturn(singletonList(todo));

        Todo updatedTodo = masterList.update(todo.getLocalIdentifier(), "someOtherTask");

        assertThat(updatedTodo).isEqualTo(new Todo(todo.getLocalIdentifier(), "someOtherTask", todo.getListName(), todo.getPosition()));
        assertThat(masterList.getAllTodos()).containsOnly(updatedTodo);
    }

    @Test
    public void update_whenTaskAlreadyExists_throwsDuplicateTodoException() throws Exception {
        Todo todo = new Todo("someId", "sameTask", MasterList.NAME, 1);
        when(mockNowList.getTodos()).thenReturn(singletonList(todo));

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
        Todo todo = new Todo("someId", "someTask", MasterList.NAME, 1);
        when(mockNowList.getTodos()).thenReturn(singletonList(todo));

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
        Todo todo = new Todo("someId", "someTask", MasterList.NAME, 1);
        when(mockNowList.getTodos()).thenReturn(singletonList(todo));

        masterList.move(todo.getLocalIdentifier(), "someOtherId");

        verify(mockNowList).getByIdentifier("someOtherId");
    }

    @Test
    public void move_whenTodoWithIdentifierExists_whenTargetNotFoundInList_throwsTodoNotFoundException() throws Exception {
        Todo todo = new Todo("someId", "someTask", MasterList.NAME, 1);
        when(mockNowList.getTodos()).thenReturn(singletonList(todo));
        when(mockNowList.getByIdentifier(any())).thenThrow(new TodoNotFoundException());

        exception.expect(TodoNotFoundException.class);
        masterList.move(todo.getLocalIdentifier(), "nonExistentIdentifier");
    }

    @Test
    public void move_whenTodoWithIdentifierExists_whenTargetFoundInList_movesTodoInMatchingList() throws TodoNotFoundException {
        Todo originalTodo = new Todo("someId", "someTask", MasterList.NAME, 1);
        when(mockNowList.getTodos()).thenReturn(singletonList(originalTodo));
        Todo targetTodo = new Todo("someOtherId", "someTask", MasterList.NAME, 2);
        when(mockNowList.getByIdentifier(any())).thenReturn(targetTodo);

        masterList.move(originalTodo.getLocalIdentifier(), "someOtherId");

        verify(mockNowList).move(originalTodo, targetTodo);
    }

    @Test
    public void isAbleToBeUnlocked_whenThereAreNoListUnlocks_returnsTrue() throws Exception {
        Instant currentInstant = Instant.ofEpochMilli(1234L);
        when(mockClock.instant()).thenReturn(currentInstant);

        Boolean ableToBeUnlocked = masterList.isAbleToBeUnlocked();

        assertThat(ableToBeUnlocked).isTrue();
    }

    @Test
    public void isAbleToBeUnlocked_whenThereAreListUnlocks_whenFirstListUnlockWasCreatedToday_returnsFalse() throws Exception {
        when(mockClock.instant()).thenReturn(Instant.ofEpochMilli(1234L));
        masterList.unlock();

        Boolean ableToBeUnlocked = masterList.isAbleToBeUnlocked();

        assertThat(ableToBeUnlocked).isFalse();
    }

    @Test
    public void isAbleToBeUnlocked_whenThereAreListUnlocks_whenFirstListUnlockWasCreatedBeforeToday_returnsTrue() throws Exception {
        when(mockClock.instant()).thenReturn(Instant.ofEpochMilli(0L));
        masterList.unlock();

        Instant currentInstant = Instant.ofEpochMilli(432143214321L);
        when(mockClock.instant()).thenReturn(currentInstant);
        Boolean ableToBeReplenished = masterList.isAbleToBeUnlocked();

        assertThat(ableToBeReplenished).isTrue();
    }

    @Test
    public void isAbleToBeUnlocked_whenThereAreListUnlocks_whenFirstListUnlockWasCreatedBeforeToday_butListIsStillUnlocked_returnsFalse() throws Exception {
        when(mockClock.instant()).thenReturn(Instant.ofEpochMilli(618536700000L)); // Monday, August 7, 1989 11:45:00 PM
        masterList.unlock();

        Instant currentInstant = Instant.ofEpochMilli(618537900000L); // Tuesday, August 8, 1989 12:05:00 AM
        when(mockClock.instant()).thenReturn(currentInstant);
        Boolean ableToBeReplenished = masterList.isAbleToBeUnlocked();

        assertThat(ableToBeReplenished).isFalse();
    }

    @Test
    public void unlock_whenListIsAbleToBeUnlockedreturnsAListUnlock_createdAtCurrentInstant() throws Exception {
        Instant currentInstant = Instant.ofEpochMilli(1234L);
        when(mockClock.instant()).thenReturn(currentInstant);

        ListUnlock listUnlock = masterList.unlock();

        assertThat(listUnlock).isNotNull();
        assertThat(listUnlock.getCreatedAt()).isEqualTo(Date.from(currentInstant));
    }

    @Test
    public void unlock_whenListIsUnableToBeUnlocked_throwsLockTimerNotExpiredException() throws Exception {
        when(mockClock.instant()).thenReturn(Instant.ofEpochMilli(1234L));
        masterList.unlock();

        exception.expect(LockTimerNotExpiredException.class);
        masterList.unlock();
    }

    @Test
    public void isLocked_whenThereAreNoListUnlocks_returnsTrue() throws Exception {
        assertThat(masterList.isLocked()).isTrue();
    }

    @Test
    public void isLocked_whenThereAreListUnlocks_whenItHasBeenMoreThan30MinutesSinceFirstUnlockCreated_returnsTrue() throws Exception {
        Instant unlockInstant = Instant.ofEpochMilli(0L);
        when(mockClock.instant()).thenReturn(unlockInstant);
        masterList.unlock();

        when(mockClock.instant()).thenReturn(unlockInstant.plusSeconds(1801L));
        assertThat(masterList.isLocked()).isTrue();
    }

    @Test
    public void isLocked_whenThereAreListUnlocks_whenItHasBeen30MinutesOrLessSinceFirstUnlockCreated_returnsFalse() throws Exception {
        Instant unlockInstant = Instant.ofEpochMilli(0L);
        when(mockClock.instant()).thenReturn(unlockInstant);
        masterList.unlock();

        when(mockClock.instant()).thenReturn(unlockInstant.plusSeconds(1799L));
        assertThat(masterList.isLocked()).isFalse();
    }

    @Test
    public void unlockDuration_whenThereAreNoListUnlocks_returns0() {
        assertThat(masterList.unlockDuration()).isEqualTo(0L);
    }

    @Test
    public void unlockDuration_whenThereAreListUnlocks_whenFirstUnlockIsNotExpired_returnsRemainingDurationRelativeToNow_inMs() throws Exception {
        when(mockClock.instant()).thenReturn(Instant.ofEpochMilli(4900000));
        masterList.unlock();

        when(mockClock.instant()).thenReturn(Instant.ofEpochMilli(5000000L));
        assertThat(masterList.unlockDuration()).isEqualTo(1700000L);
    }

    @Test
    public void unlockDuration_whenThereAreListUnlocks_whenFirstUnlockIsExpired_returns0() throws Exception {
        when(mockClock.instant()).thenReturn(Instant.ofEpochMilli(3000000L));
        masterList.unlock();

        when(mockClock.instant()).thenReturn(Instant.ofEpochMilli(5000000L));
        assertThat(masterList.unlockDuration()).isEqualTo(0L);
    }

    @Test
    public void pull_whenThereAreNoImmediateTodos_fillsFromPostponedList() throws Exception {
        MasterList masterList = new MasterList(mockClock, new UniqueIdentifier<>("something"), emptyList());
        Todo firstLater = masterList.addDeferred("firstLater");
        Todo secondLater = masterList.addDeferred("secondLater");

        List<Todo> effectedTodos = masterList.pull();

        assertThat(effectedTodos).hasSize(2);
        Todo pulledTodo1 = effectedTodos.get(0);
        assertThat(pulledTodo1.getTask()).isEqualTo(firstLater.getTask());
        Todo pulledTodo2 = effectedTodos.get(1);
        assertThat(pulledTodo2.getTask()).isEqualTo(secondLater.getTask());
        assertThat(masterList.getTodos()).containsExactly(pulledTodo1, pulledTodo2);
    }

    @Test
    public void pull_whenThereAreLessImmediateTodosThanMaxSize_fillsFromPostponedList_withAsManyTodosAsTheDifference() throws Exception {
        MasterList masterList = new MasterList(mockClock, new UniqueIdentifier<>("something"), Collections.emptyList());
        Todo firstNow = masterList.add("firstNow");
        Todo firstLater = masterList.addDeferred("firstLater");
        masterList.addDeferred("secondLater");
        masterList.addDeferred("thirdLater");

        List<Todo> effectedTodos = masterList.pull();

        assertThat(effectedTodos).hasSize(1);
        Todo pulledTodo = effectedTodos.get(0);
        assertThat(pulledTodo.getTask()).isEqualTo(firstLater.getTask());
        assertThat(masterList.getTodos()).containsExactly(firstNow, pulledTodo);
    }

    @Test
    public void pull_whenThereAreAsManyImmediateTodosAsMaxSize_doesNotFillFromPostponedList() throws Exception {
        MasterList masterList = new MasterList(mockClock, new UniqueIdentifier<>("something"), Collections.emptyList());
        masterList.add("someTask");
        masterList.add("someOtherTask");
        masterList.addDeferred("firstLater");

        List<Todo> todos = masterList.pull();

        assertThat(todos).isEmpty();
    }

    @Test
    public void pull_whenThereIsASourceList_whenThereAreLessTodosThanMaxSize_whenSourceListIsEmpty_doesNotFillListFromSource() throws Exception {
        MasterList masterList = new MasterList(mockClock, new UniqueIdentifier<>("something"), emptyList());

        List<Todo> todos = masterList.pull();

        assertThat(todos).isEmpty();
        assertThat(masterList.getTodos()).isEmpty();
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
        Instant unlockInstant = Instant.ofEpochMilli(0L);
        when(mockClock.instant()).thenReturn(unlockInstant);
        masterList.unlock();

        when(mockClock.instant()).thenReturn(unlockInstant.plusSeconds(1799L));
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
        when(mockLaterList.getTodos()).thenReturn(singletonList(new Todo("someTask", MasterList.DEFERRED_NAME, 1)));

        boolean hasDeferredTodosAvailable = masterList.isAbleToBeReplenished();

        assertThat(hasDeferredTodosAvailable).isTrue();
    }

    @Test
    public void isAbleToBeReplenished_whenThereAreDeferredTodos_andTheListIsFull_returnsFalse() {
        when(mockNowList.isFull()).thenReturn(true);
        when(mockLaterList.getTodos()).thenReturn(singletonList(new Todo("someTask", MasterList.DEFERRED_NAME, 1)));

        boolean hasDeferredTodosAvailable = masterList.isAbleToBeReplenished();

        assertThat(hasDeferredTodosAvailable).isFalse();
    }
}