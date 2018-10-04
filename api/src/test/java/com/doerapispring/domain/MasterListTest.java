package com.doerapispring.domain;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class MasterListTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();
    private IMasterList masterList;
    private Clock mockClock;
    private UniqueIdentifier<String> uniqueIdentifier;

    @Before
    public void setUp() throws Exception {
        mockClock = mock(Clock.class);

        when(mockClock.getZone()).thenReturn(ZoneId.of("UTC"));
        when(mockClock.instant()).thenAnswer(invocation -> Instant.now());

        uniqueIdentifier = new UniqueIdentifier<>("something");
        masterList = new MasterList(
            mockClock,
            uniqueIdentifier,
            null,
            new ArrayList<>(),
            0);
    }

    @Test
    public void add_addsToNowList() throws Exception {
        masterList.add(new TodoId("someId"), "someTask");

        assertThat(masterList.getTodos()).containsExactly(new Todo(new TodoId("someId"), "someTask"));
    }

    @Test
    public void add_addsToNowList_beforeFirstTodo() throws Exception {
        masterList.add(new TodoId("someId"), "someTask");
        masterList.add(new TodoId("someId"), "someOtherTask");

        assertThat(masterList.getTodos()).extracting("task")
            .containsExactly("someOtherTask", "someTask");
    }

    @Test
    public void add_whenListAlreadyContainsTask_doesNotAdd_throwsDuplicateTodoException() throws Exception {
        masterList.add(new TodoId("someId"), "sameTask");

        exception.expect(DuplicateTodoException.class);
        masterList.add(new TodoId("someId"), "sameTask");
    }

    @Test
    public void add_whenListIsFull_doesNotAdd_throwsListSizeExceededException() throws Exception {
        masterList.add(new TodoId("someId"), "someTask");
        masterList.add(new TodoId("someId"), "someOtherTask");

        exception.expect(ListSizeExceededException.class);
        masterList.add(new TodoId("someId"), "stillAnotherTask");
    }

    @Test
    public void addDeferred_addsToLaterList() throws Exception {
        masterList.addDeferred(new TodoId("someId"), "someTask");

        masterList.unlock();
        assertThat(masterList.getDeferredTodos()).containsExactly(new Todo(new TodoId("someId"), "someTask"));
    }

    @Test
    public void addDeferred_addsToLaterList_afterLastTodo() throws Exception {
        masterList.addDeferred(new TodoId("someId"), "someTask");
        masterList.addDeferred(new TodoId("someId"), "someOtherTask");

        masterList.unlock();
        assertThat(masterList.getDeferredTodos()).extracting("task")
            .containsExactly("someTask", "someOtherTask");
    }

    @Test
    public void addDeferred_whenListAlreadyContainsTask_doesNotAdd_throwsDuplicateTodoException() throws Exception {
        masterList.addDeferred(new TodoId("someId"), "sameTask");

        exception.expect(DuplicateTodoException.class);
        masterList.addDeferred(new TodoId("someId"), "sameTask");
    }

    @Test
    public void delete_whenTodoWithIdentifierExists_removesTodo() throws Exception {
        masterList.add(new TodoId("someId"), "someTask");
        Todo todo = masterList.getTodos().get(0);

        masterList.delete(todo.getLocalIdentifier());

        assertThat(masterList.getTodos()).isEmpty();
    }

    @Test
    public void delete_whenTodoWithIdentifierExists_removesDeferredTodo() throws Exception {
        masterList.addDeferred(new TodoId("someId"), "someTask");
        masterList.unlock();
        Todo todo = masterList.getDeferredTodos().get(0);

        masterList.delete(todo.getLocalIdentifier());

        assertThat(masterList.getDeferredTodos()).isEmpty();
    }

    @Test
    public void delete_whenTodoWithIdentifierDoesNotExist_throwsNotFoundException() throws Exception {
        exception.expect(TodoNotFoundException.class);
        masterList.delete("someBogusIdentifier");
    }

    @Test
    public void displace_whenListIsNotFull_throwsListNotFullException() throws Exception {
        exception.expect(ListNotFullException.class);
        masterList.displace(null, "someTask");
    }

    @Test
    public void displace_whenListIsFull_whenTaskAlreadyExists_throwsDuplicateTodoException() throws Exception {
        masterList.add(new TodoId("someId"), "task");
        masterList.add(new TodoId("someId"), "sameTask");

        exception.expect(DuplicateTodoException.class);
        masterList.displace(new TodoId("someId"), "sameTask");
    }

    @Test
    public void displace_whenPostponedListIsEmpty_replacesTodo_andPushesItIntoPostponedListWithCorrectPositioning() throws Exception {
        masterList.add(new TodoId("1"), "someNowTask");
        masterList.add(new TodoId("2"), "someOtherNowTask");

        masterList.displace(new TodoId("3"), "displace it");

        assertThat(masterList.getTodos()).containsExactly(
            new Todo(new TodoId("3"), "displace it"),
            new Todo(new TodoId("2"), "someOtherNowTask"));
        masterList.unlock();
        assertThat(masterList.getDeferredTodos()).containsExactly(
            new Todo(new TodoId("1"), "someNowTask"));
    }

    @Test
    public void displace_whenPostponedIsNotEmpty_replacesTodo_andPushesItIntoPostponedListWithCorrectPositioning() throws Exception {
        masterList.add(new TodoId("1"), "someNowTask");
        masterList.add(new TodoId("2"), "someOtherNowTask");
        masterList.addDeferred(new TodoId("3"), "someLaterTask");

        masterList.displace(new TodoId("4"), "displace it");

        assertThat(masterList.getTodos()).containsExactly(
            new Todo(new TodoId("4"), "displace it"),
            new Todo(new TodoId("2"), "someOtherNowTask"));
        masterList.unlock();
        assertThat(masterList.getDeferredTodos()).containsExactly(
            new Todo(new TodoId("1"), "someNowTask"),
            new Todo(new TodoId("3"), "someLaterTask"));
    }

    @Test
    public void update_whenTodoWithIdentifierExists_updatesTodo() throws Exception {
        masterList.add(new TodoId("someId"), "someTask");
        Todo todo = masterList.getTodos().get(0);

        masterList.update(todo.getLocalIdentifier(), "someOtherTask");

        assertThat(todo.getTask()).isEqualTo("someOtherTask");
    }

    @Test
    public void update_whenTaskAlreadyExists_throwsDuplicateTodoException() throws Exception {
        masterList.add(new TodoId("someId"), "sameTask");
        Todo todo = masterList.getTodos().get(0);

        exception.expect(DuplicateTodoException.class);
        masterList.update(todo.getLocalIdentifier(), "sameTask");
    }

    @Test
    public void update_whenTodoWithIdentifierDoesNotExist_throwsNotFoundException() throws Exception {
        exception.expect(TodoNotFoundException.class);
        masterList.update("bananaPudding", "sameTask");
    }

    @Test
    public void complete_whenTodoWithIdentifierExists_removesTodoFromMatchingList_returnsCompletedTask() throws Exception {
        masterList.add(new TodoId("someId"), "someTask");
        Todo todo = masterList.getTodos().get(0);

        String completedTask = masterList.complete(todo.getLocalIdentifier());

        assertThat(masterList.getTodos()).isEmpty();
        assertThat(completedTask).isEqualTo("someTask");
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
    public void move_whenTodoWithIdentifierExists_whenTargetExists_movesTodoDown() throws Exception {
        masterList.add(new TodoId("someId"), "now1");
        masterList.add(new TodoId("someId"), "now2");

        List<String> tasks = Arrays.asList(
            "someTask",
            "anotherTask",
            "yetAnotherTask",
            "evenYetAnotherTask");

        for (int i = 0; i < tasks.size(); i++) {
            masterList.addDeferred(new TodoId(String.valueOf(i)), tasks.get(i));
        }
        masterList.unlock();
        Todo firstTodo = masterList.getDeferredTodos().get(0);
        Todo thirdTodo = masterList.getDeferredTodos().get(2);

        masterList.move(firstTodo.getLocalIdentifier(), thirdTodo.getLocalIdentifier());

        assertThat(masterList.getDeferredTodos()).extracting("task").containsExactly(
            "anotherTask",
            "yetAnotherTask",
            "someTask",
            "evenYetAnotherTask");
    }

    @Test
    public void move_whenTodoWithIdentifierExists_whenTargetExists_movesTodoUp() throws Exception {
        masterList.add(new TodoId("someId"), "now1");
        masterList.add(new TodoId("someId"), "now2");

        List<String> tasks = asList(
            "someTask",
            "anotherTask",
            "yetAnotherTask",
            "evenYetAnotherTask");

        for (int i = 0; i < tasks.size(); i++) {
            masterList.addDeferred(new TodoId(String.valueOf(i)), tasks.get(i));
        }
        masterList.unlock();
        Todo secondTodo = masterList.getDeferredTodos().get(1);
        Todo fourthTodo = masterList.getDeferredTodos().get(3);

        masterList.move(fourthTodo.getLocalIdentifier(), secondTodo.getLocalIdentifier());

        assertThat(masterList.getDeferredTodos()).extracting("task").containsExactly(
            "someTask",
            "evenYetAnotherTask",
            "anotherTask",
            "yetAnotherTask");
    }

    @Test
    public void move_beforeOrAfter_whenTodoWithIdentifierExists_whenTargetExists_whenOriginalAndTargetPositionsAreSame_doesNothing() throws Exception {
        List<String> tasks = asList(
            "someTask",
            "anotherTask",
            "yetAnotherTask",
            "evenYetAnotherTask"
        );

        for (String task : tasks) {
            masterList.addDeferred(new TodoId("someId"), task);
        }
        masterList.unlock();
        Todo firstTodo = masterList.getDeferredTodos().get(0);

        masterList.move(firstTodo.getLocalIdentifier(), firstTodo.getLocalIdentifier());

        assertThat(masterList.getDeferredTodos()).extracting("task").isEqualTo(tasks);
    }

    @Test
    public void isAbleToBeUnlocked_whenThereAreNoListUnlocks_returnsTrue() throws Exception {
        Boolean ableToBeUnlocked = masterList.isAbleToBeUnlocked();

        assertThat(ableToBeUnlocked).isTrue();
    }

    @Test
    public void isAbleToBeUnlocked_whenThereAreListUnlocks_whenFirstListUnlockWasCreatedToday_returnsFalse() throws Exception {
        when(mockClock.instant()).thenReturn(Instant.ofEpochMilli(618537600000L)); // Tuesday, August 8, 1989 12:00:00 AM
        masterList.unlock();

        when(mockClock.instant()).thenReturn(Instant.ofEpochMilli(618623999999L)); // Tuesday, August 8, 1989 11:59:59 PM
        Boolean ableToBeUnlocked = masterList.isAbleToBeUnlocked();

        assertThat(ableToBeUnlocked).isFalse();
    }

    @Test
    public void isAbleToBeUnlocked_whenThereAreListUnlocks_whenFirstListUnlockWasCreatedBeforeToday_returnsTrue() throws Exception {
        when(mockClock.instant()).thenReturn(Instant.ofEpochMilli(618429599999L)); // Monday, August 7, 1989 11:29:59 PM
        masterList.unlock();

        when(mockClock.instant()).thenReturn(Instant.ofEpochMilli(618537600000L)); // Tuesday, August 8, 1989 12:00:00 AM
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
    public void unlock_whenListIsAbleToBeUnlocked_unlocksTheList() throws Exception {
        Instant currentInstant = Instant.ofEpochMilli(1234L);
        when(mockClock.instant()).thenReturn(currentInstant);

        assertThat(masterList.isLocked()).isTrue();

        masterList.unlock();

        assertThat(masterList.isLocked()).isFalse();
    }

    @Test
    public void unlock_whenListIsUnableToBeUnlocked_throwsLockTimerNotExpiredException() throws Exception {
        when(mockClock.instant()).thenReturn(Instant.ofEpochMilli(1234L));
        masterList.unlock();

        exception.expect(LockTimerNotExpiredException.class);
        masterList.unlock();
    }

    @Test
    public void isLocked_returnsTrueByDefault() throws Exception {
        assertThat(masterList.isLocked()).isTrue();
    }

    @Test
    public void isLocked_whenItHasBeenMoreThan30MinutesSinceLastUnlock_returnsTrue() throws Exception {
        Instant unlockInstant = Instant.ofEpochMilli(0L);
        when(mockClock.instant()).thenReturn(unlockInstant);
        masterList.unlock();

        when(mockClock.instant()).thenReturn(unlockInstant.plusSeconds(1801L));
        assertThat(masterList.isLocked()).isTrue();
    }

    @Test
    public void isLocked_whenItHasBeen30MinutesOrLessSinceLastUnlock_returnsFalse() throws Exception {
        Instant unlockInstant = Instant.ofEpochMilli(0L);
        when(mockClock.instant()).thenReturn(unlockInstant);
        masterList.unlock();

        when(mockClock.instant()).thenReturn(unlockInstant.plusSeconds(1800L));
        assertThat(masterList.isLocked()).isFalse();
    }

    @Test
    public void unlockDuration_whenListHasNeverBeenUnlocked_returns0() {
        assertThat(masterList.unlockDuration()).isEqualTo(0L);
    }

    @Test
    public void unlockDuration_whenListIsCurrentlyUnlocked_returnsRemainingDurationRelativeToNow_inMs() throws Exception {
        when(mockClock.instant()).thenReturn(Instant.ofEpochMilli(4900000));
        masterList.unlock();

        when(mockClock.instant()).thenReturn(Instant.ofEpochMilli(5000000L));
        assertThat(masterList.unlockDuration()).isEqualTo(1700000L);
    }

    @Test
    public void unlockDuration_whenListIsCurrentlyLocked_returns0() throws Exception {
        when(mockClock.instant()).thenReturn(Instant.ofEpochMilli(3000000L));
        masterList.unlock();

        when(mockClock.instant()).thenReturn(Instant.ofEpochMilli(5000000L));
        assertThat(masterList.unlockDuration()).isEqualTo(0L);
    }

    @Test
    public void pull_whenThereAreNoImmediateTodos_fillsFromPostponedList() throws Exception {
        masterList.addDeferred(new TodoId("someId"), "firstLater");
        masterList.addDeferred(new TodoId("someId"), "secondLater");

        masterList.pull();

        assertThat(masterList.getTodos()).extracting("task")
            .containsExactly("firstLater", "secondLater");
    }

    @Test
    public void pull_whenThereAreNoImmediateTodos_andOneDeferredTodo_fillsFromPostponedList() throws Exception {
        masterList.addDeferred(new TodoId("someId"), "firstLater");

        masterList.pull();

        assertThat(masterList.getTodos()).extracting("task")
            .containsExactly("firstLater");
    }

    @Test
    public void pull_whenThereAreLessImmediateTodosThanMaxSize_fillsFromPostponedList_withAsManyTodosAsTheDifference() throws Exception {
        masterList.add(new TodoId("someId"), "firstNow");
        masterList.addDeferred(new TodoId("someId"), "firstLater");
        masterList.addDeferred(new TodoId("someId"), "secondLater");
        masterList.addDeferred(new TodoId("someId"), "thirdLater");

        masterList.pull();

        assertThat(masterList.getTodos()).extracting("task")
            .containsExactly("firstNow", "firstLater");
    }

    @Test
    public void pull_whenThereAreAsManyImmediateTodosAsMaxSize_doesNotFillFromPostponedList() throws Exception {
        masterList.add(new TodoId("someId"), "someTask");
        masterList.add(new TodoId("someId"), "someOtherTask");
        masterList.addDeferred(new TodoId("someId"), "firstLater");

        masterList.pull();

        assertThat(masterList.getTodos()).extracting("task")
            .containsExactly("someOtherTask", "someTask");
    }

    @Test
    public void pull_whenThereIsASourceList_whenThereAreLessTodosThanMaxSize_whenSourceListIsEmpty_doesNotFillListFromSource() throws Exception {
        masterList.pull();

        assertThat(masterList.getTodos()).isEmpty();
    }

    @Test
    public void getTodos_getsTodosFromImmediateList() throws Exception {
        masterList.add(new TodoId("someId"), "someTask");

        assertThat(masterList.getTodos()).extracting("task")
            .contains("someTask");
    }

    @Test
    public void getTodos_doesNotGetTodosFromPostponedList() throws Exception {
        masterList.addDeferred(new TodoId("someId"), "someTask");

        assertThat(masterList.getTodos()).isEmpty();
    }

    @Test
    public void getDeferredTodos_getsTodosFromPostponedList() throws Exception {
        masterList.addDeferred(new TodoId("someId"), "someTask");

        masterList.unlock();
        assertThat(masterList.getDeferredTodos()).extracting("task")
            .contains("someTask");
    }

    @Test
    public void getDeferredTodos_doesNotGetTodosFromImmediateList() throws Exception {
        masterList.add(new TodoId("someId"), "someTask");

        masterList.unlock();
        assertThat(masterList.getDeferredTodos()).isEmpty();
    }

    @Test
    public void getDeferredTodos_whenListIsLocked_returnEmptyList() throws Exception {
        masterList.addDeferred(new TodoId("someId"), "someTask");

        List<Todo> deferredTodos = masterList.getDeferredTodos();

        assertThat(deferredTodos).isEmpty();
    }

    @Test
    public void getDeferredTodos_whenListIsNotLocked_getsTodosFromPostponedList() throws Exception {
        Instant unlockInstant = Instant.ofEpochMilli(0L);
        when(mockClock.instant()).thenReturn(unlockInstant);
        masterList.unlock();

        masterList.addDeferred(new TodoId("someId"), "someTask");

        when(mockClock.instant()).thenReturn(unlockInstant.plusSeconds(1799L));
        assertThat(masterList.getDeferredTodos()).extracting("task")
            .contains("someTask");
    }

    @Test
    public void isFull_whenCountOfTodos_isGreaterThanOrEqualToMaxSize_returnsTrue() throws Exception {
        masterList.add(new TodoId("someId"), "todo1");
        masterList.add(new TodoId("someId"), "todo2");

        assertThat(masterList.isFull()).isTrue();
    }

    @Test
    public void isFull_whenCountOfTodos_isLessThanMaxSize_returnsFalse() throws Exception {
        assertThat(masterList.isFull()).isEqualTo(false);
    }

    @Test
    public void isAbleToBeReplenished_whenThereAreDeferredTodos_andTheListIsNotFull_returnsTrue() throws Exception {
        masterList.addDeferred(new TodoId("someId"), "someTask");

        boolean hasDeferredTodosAvailable = masterList.isAbleToBeReplenished();

        assertThat(hasDeferredTodosAvailable).isTrue();
    }

    @Test
    public void isAbleToBeReplenished_whenThereAreDeferredTodos_andTheListIsFull_returnsFalse() throws Exception {
        masterList.add(new TodoId("someId"), "todo1");
        masterList.add(new TodoId("someId"), "todo2");
        masterList.addDeferred(new TodoId("someId"), "someTask");

        boolean hasDeferredTodosAvailable = masterList.isAbleToBeReplenished();

        assertThat(hasDeferredTodosAvailable).isFalse();
    }

    @Test
    public void isAbleToBeReplenished_whenThereAreNoDeferredTodos_andTheListIsNotFull_returnsFalse() throws Exception {
        masterList.add(new TodoId("someId"), "todo1");

        boolean hasDeferredTodosAvailable = masterList.isAbleToBeReplenished();

        assertThat(hasDeferredTodosAvailable).isFalse();
    }
}