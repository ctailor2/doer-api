package com.doerapispring.domain;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MasterListTest {
    private MasterList masterList;

    private Clock mockClock;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        mockClock = mock(Clock.class);

        when(mockClock.getZone()).thenReturn(ZoneId.systemDefault());
        when(mockClock.instant()).thenAnswer(invocation -> Instant.now());

        masterList = new MasterList(mockClock, new UniqueIdentifier<>("something"), new ArrayList<>());
    }

    @Test
    public void add_addsToNowList() throws Exception {
        Todo todo = masterList.add("someTask");

        assertThat(masterList.getTodos()).contains(todo);
    }

    @Test
    public void add_whenListAlreadyContainsTask_doesNotAdd_throwsDuplicateTodoException() throws Exception {
        masterList.add("sameTask");

        exception.expect(DuplicateTodoException.class);
        masterList.add("sameTask");
    }

    @Test
    public void addDeferred_addsToLaterList() throws Exception {
        Todo todo = masterList.addDeferred("someTask");

        masterList.unlock();
        assertThat(masterList.getDeferredTodos()).contains(todo);
    }

    @Test
    public void addDeferred_whenListAlreadyContainsTask_doesNotAdd_throwsDuplicateTodoException() throws Exception {
        masterList.addDeferred("sameTask");

        exception.expect(DuplicateTodoException.class);
        masterList.addDeferred("sameTask");
    }

    @Test
    public void delete_whenTodoWithIdentifierExists_removesTodoFromMatchingList() throws Exception {
        Todo todo = masterList.addDeferred("someTask");

        Todo deletedTodo = masterList.delete(todo.getLocalIdentifier());

        assertThat(deletedTodo).isEqualTo(todo);
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
        Todo todo = masterList.add("sameTask");

        exception.expect(DuplicateTodoException.class);
        masterList.displace(todo.getLocalIdentifier(), "sameTask");
    }

    @Test
    public void displace_whenTodoWithIdentifierExists_whenPostponedListIsEmpty_replacesTodo_andPushesItIntoPostponedListWithCorrectPositioning() throws Exception {
        Todo nowTodo = masterList.add("someNowTask");
        masterList.unlock();

        Todo todo = masterList.displace(nowTodo.getLocalIdentifier(), "displace it");

        assertThat(todo.getTask()).isEqualTo("displace it");
        assertThat(todo.getPosition()).isEqualTo(nowTodo.getPosition());
        assertThat(todo.getListName()).isEqualTo(MasterList.NAME);
        assertThat(masterList.getTodos()).containsExactly(todo);
        assertThat(masterList.getDeferredTodos()).containsExactly(
            new Todo(
                nowTodo.getLocalIdentifier(),
                nowTodo.getTask(),
                MasterList.DEFERRED_NAME,
                1));
    }

    @Test
    public void displace_whenTodoWithIdentifierExists_whenPostponedIsNotEmpty_replacesTodo_andPushesItIntoPostponedListWithCorrectPositioning() throws Exception {
        Todo nowTodo1 = masterList.add("someNowTask");
        Todo nowTodo2 = masterList.add("someOtherNowTask");
        Todo laterTodo = masterList.addDeferred("someLaterTask");
        masterList.unlock();

        Todo todo = masterList.displace(nowTodo1.getLocalIdentifier(), "displace it");

        assertThat(todo.getTask()).isEqualTo("displace it");
        assertThat(todo.getPosition()).isEqualTo(nowTodo1.getPosition());
        assertThat(todo.getListName()).isEqualTo(MasterList.NAME);
        assertThat(masterList.getTodos()).containsExactly(todo, nowTodo2);
        assertThat(masterList.getDeferredTodos()).containsExactly(
            new Todo(
                nowTodo1.getLocalIdentifier(),
                nowTodo1.getTask(),
                MasterList.DEFERRED_NAME,
                laterTodo.getPosition() - 1),
            laterTodo);
    }

    @Test
    public void update_whenTodoWithIdentifierExists_updatesTodo() throws Exception {
        Todo todo = masterList.add("someTask");

        Todo updatedTodo = masterList.update(todo.getLocalIdentifier(), "someOtherTask");

        assertThat(updatedTodo).isEqualTo(new Todo(todo.getLocalIdentifier(), "someOtherTask", todo.getListName(), todo.getPosition()));
        assertThat(masterList.getTodos()).containsOnly(updatedTodo);
    }

    @Test
    public void update_whenTaskAlreadyExists_throwsDuplicateTodoException() throws Exception {
        Todo todo = masterList.add("sameTask");

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
        Todo todo = masterList.add("someTask");

        Todo completedTodo = masterList.complete(todo.getLocalIdentifier());

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
    public void move_whenTodoWithIdentifierExists_whenTargetFoundInList_movesTodoInMatchingList() throws Exception {
        Todo originalTodo = masterList.add("someTask");
        Todo targetTodo = masterList.add("someOtherTask");

        masterList.move(originalTodo.getLocalIdentifier(), targetTodo.getLocalIdentifier());

        assertThat(masterList.getTodos()).containsExactly(targetTodo, originalTodo);
    }

    @Test
    public void isAbleToBeUnlocked_whenThereAreNoListUnlocks_returnsTrue() throws Exception {
        Boolean ableToBeUnlocked = masterList.isAbleToBeUnlocked();

        assertThat(ableToBeUnlocked).isTrue();
    }

    @Test
    public void isAbleToBeUnlocked_whenThereAreListUnlocks_whenFirstListUnlockWasCreatedToday_returnsFalse() throws Exception {
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
    public void unlock_whenListIsAbleToBeUnlockedReturnsAListUnlock_createdAtCurrentInstant() throws Exception {
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
        masterList.add("someTask");
        masterList.add("someOtherTask");
        masterList.addDeferred("firstLater");

        List<Todo> todos = masterList.pull();

        assertThat(todos).isEmpty();
    }

    @Test
    public void pull_whenThereIsASourceList_whenThereAreLessTodosThanMaxSize_whenSourceListIsEmpty_doesNotFillListFromSource() throws Exception {
        List<Todo> todos = masterList.pull();

        assertThat(todos).isEmpty();
        assertThat(masterList.getTodos()).isEmpty();
    }

    @Test
    public void getTodos_getsTodosFromImmediateList() throws Exception {
        Todo todo = masterList.add("someTask");

        masterList.getTodos();

        assertThat(masterList.getTodos()).contains(todo);
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

        Todo todo = masterList.addDeferred("someTask");

        when(mockClock.instant()).thenReturn(unlockInstant.plusSeconds(1799L));
        assertThat(masterList.getDeferredTodos()).contains(todo);
    }

    @Test
    public void isFull() throws Exception {
        masterList.add("todo1");
        masterList.add("todo2");

        assertThat(masterList.isFull()).isTrue();
    }

    @Test
    public void isAbleToBeReplenished_whenThereAreDeferredTodos_andTheListIsNotFull_returnsTrue() throws Exception {
        masterList.addDeferred("someTask");

        boolean hasDeferredTodosAvailable = masterList.isAbleToBeReplenished();

        assertThat(hasDeferredTodosAvailable).isTrue();
    }

    @Test
    public void isAbleToBeReplenished_whenThereAreDeferredTodos_andTheListIsFull_returnsFalse() throws Exception {
        masterList.add("todo1");
        masterList.add("todo2");
        masterList.addDeferred("someTask");

        boolean hasDeferredTodosAvailable = masterList.isAbleToBeReplenished();

        assertThat(hasDeferredTodosAvailable).isFalse();
    }
}