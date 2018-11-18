package com.doerapispring.domain;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.sql.Date;
import java.time.Clock;
import java.time.Instant;
import java.time.Period;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TodoListTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();
    private TodoList todoList;
    private Clock mockClock;
    private UserId userId;

    @Before
    public void setUp() throws Exception {
        mockClock = mock(Clock.class);

        when(mockClock.getZone()).thenReturn(ZoneId.of("UTC"));
        when(mockClock.instant()).thenAnswer(invocation -> Instant.now());

        userId = new UserId("something");
        todoList = new TodoList(
            mockClock,
            userId,
            Date.from(Instant.EPOCH),
            new ArrayList<>(),
            0);
    }

    @Test
    public void add_addsToNowList() throws Exception {
        todoList.add(new TodoId("someId"), "someTask");

        assertThat(todoList.read().getTodos()).containsExactly(new Todo(new TodoId("someId"), "someTask"));
    }

    @Test
    public void add_addsToNowList_beforeFirstTodo() throws Exception {
        todoList.add(new TodoId("someId"), "someTask");
        todoList.add(new TodoId("someId"), "someOtherTask");

        assertThat(todoList.read().getTodos()).extracting("task")
            .containsExactly("someOtherTask", "someTask");
    }

    @Test
    public void add_whenListAlreadyContainsTask_doesNotAdd_throwsDuplicateTodoException() throws Exception {
        todoList.add(new TodoId("someId"), "sameTask");

        exception.expect(DuplicateTodoException.class);
        todoList.add(new TodoId("someId"), "sameTask");
    }

    @Test
    public void add_whenListIsFull_doesNotAdd_throwsListSizeExceededException() throws Exception {
        todoList.add(new TodoId("someId"), "someTask");
        todoList.add(new TodoId("someId"), "someOtherTask");

        exception.expect(ListSizeExceededException.class);
        todoList.add(new TodoId("someId"), "stillAnotherTask");
    }

    @Test
    public void addDeferred_addsToLaterList() throws Exception {
        todoList.addDeferred(new TodoId("someId"), "someTask");

        todoList.unlock();
        assertThat(todoList.read().getDeferredTodos()).containsExactly(new Todo(new TodoId("someId"), "someTask"));
    }

    @Test
    public void addDeferred_addsToLaterList_afterLastTodo() throws Exception {
        todoList.addDeferred(new TodoId("someId"), "someTask");
        todoList.addDeferred(new TodoId("someId"), "someOtherTask");

        todoList.unlock();
        assertThat(todoList.read().getDeferredTodos()).extracting("task")
            .containsExactly("someTask", "someOtherTask");
    }

    @Test
    public void addDeferred_whenListAlreadyContainsTask_doesNotAdd_throwsDuplicateTodoException() throws Exception {
        todoList.addDeferred(new TodoId("someId"), "sameTask");

        exception.expect(DuplicateTodoException.class);
        todoList.addDeferred(new TodoId("someId"), "sameTask");
    }

    @Test
    public void delete_whenTodoWithIdentifierExists_removesTodo() throws Exception {
        TodoId todoId = new TodoId("someId");
        todoList.add(todoId, "someTask");

        todoList.delete(todoId);

        assertThat(todoList.read().getTodos()).isEmpty();
    }

    @Test
    public void delete_whenTodoWithIdentifierExists_removesDeferredTodo() throws Exception {
        TodoId todoId = new TodoId("someId");
        todoList.addDeferred(todoId, "someTask");
        todoList.unlock();

        todoList.delete(todoId);

        assertThat(todoList.read().getDeferredTodos()).isEmpty();
    }

    @Test
    public void delete_whenTodoWithIdentifierDoesNotExist_throwsNotFoundException() throws Exception {
        exception.expect(TodoNotFoundException.class);
        todoList.delete(new TodoId("someBogusIdentifier"));
    }

    @Test
    public void displace_whenListIsNotFull_throwsListNotFullException() throws Exception {
        exception.expect(ListNotFullException.class);
        todoList.displace(null, "someTask");
    }

    @Test
    public void displace_whenListIsFull_whenTaskAlreadyExists_throwsDuplicateTodoException() throws Exception {
        todoList.add(new TodoId("someId"), "task");
        todoList.add(new TodoId("someId"), "sameTask");

        exception.expect(DuplicateTodoException.class);
        todoList.displace(new TodoId("someId"), "sameTask");
    }

    @Test
    public void displace_whenPostponedListIsEmpty_replacesTodo_andPushesItIntoPostponedListWithCorrectPositioning() throws Exception {
        todoList.add(new TodoId("1"), "someNowTask");
        todoList.add(new TodoId("2"), "someOtherNowTask");

        todoList.displace(new TodoId("3"), "displace it");

        assertThat(todoList.read().getTodos()).containsExactly(
            new Todo(new TodoId("3"), "displace it"),
            new Todo(new TodoId("2"), "someOtherNowTask"));
        todoList.unlock();
        assertThat(todoList.read().getDeferredTodos()).containsExactly(
            new Todo(new TodoId("1"), "someNowTask"));
    }

    @Test
    public void displace_whenPostponedIsNotEmpty_replacesTodo_andPushesItIntoPostponedListWithCorrectPositioning() throws Exception {
        todoList.add(new TodoId("1"), "someNowTask");
        todoList.add(new TodoId("2"), "someOtherNowTask");
        todoList.addDeferred(new TodoId("3"), "someLaterTask");

        todoList.displace(new TodoId("4"), "displace it");

        assertThat(todoList.read().getTodos()).containsExactly(
            new Todo(new TodoId("4"), "displace it"),
            new Todo(new TodoId("2"), "someOtherNowTask"));
        todoList.unlock();
        assertThat(todoList.read().getDeferredTodos()).containsExactly(
            new Todo(new TodoId("1"), "someNowTask"),
            new Todo(new TodoId("3"), "someLaterTask"));
    }

    @Test
    public void update_whenTodoWithIdentifierExists_updatesTodo() throws Exception {
        TodoId todoId = new TodoId("someId");
        todoList.add(todoId, "someTask");

        todoList.update(todoId, "someOtherTask");

        Todo todo = todoList.read().getTodos().get(0);
        assertThat(todo.getTask()).isEqualTo("someOtherTask");
    }

    @Test
    public void update_whenTaskAlreadyExists_throwsDuplicateTodoException() throws Exception {
        TodoId todoId = new TodoId("someId");
        todoList.add(todoId, "sameTask");

        exception.expect(DuplicateTodoException.class);
        todoList.update(todoId, "sameTask");
    }

    @Test
    public void update_whenTodoWithIdentifierDoesNotExist_throwsNotFoundException() throws Exception {
        exception.expect(TodoNotFoundException.class);
        todoList.update(new TodoId("bananaPudding"), "sameTask");
    }

    @Test
    public void complete_whenTodoWithIdentifierExists_removesTodoFromMatchingList_returnsCompletedTodo() throws Exception {
        Instant now = Instant.now();
        when(mockClock.instant()).thenReturn(now);

        TodoId todoId = new TodoId("someId");
        todoList.add(todoId, "someTask");

        CompletedTodo completedTodo = todoList.complete(todoId);

        assertThat(todoList.read().getTodos()).isEmpty();
        assertThat(completedTodo.getUserId()).isEqualTo(userId);
        assertThat(completedTodo.getCompletedTodoId()).isEqualTo(new CompletedTodoId("someId"));
        assertThat(completedTodo.getTask()).isEqualTo("someTask");
        assertThat(completedTodo.getCompletedAt()).isEqualTo(Date.from(now));
    }

    @Test
    public void complete_whenTodoWithIdentifierDoesNotExist_throwsNotFoundException() throws Exception {
        exception.expect(TodoNotFoundException.class);
        todoList.complete(new TodoId("someBogusIdentifier"));
    }

    @Test
    public void move_whenTodoWithIdentifierDoesNotExist_throwsNotFoundException() throws Exception {
        exception.expect(TodoNotFoundException.class);
        todoList.move(new TodoId("junk"), new TodoId("bogus"));
    }

    @Test
    public void move_whenTodoWithIdentifierExists_whenTargetExists_movesTodoDown() throws Exception {
        todoList.add(new TodoId("someId"), "now1");
        todoList.add(new TodoId("someId"), "now2");

        List<String> tasks = Arrays.asList(
            "someTask",
            "anotherTask",
            "yetAnotherTask",
            "evenYetAnotherTask");

        for (int i = 0; i < tasks.size(); i++) {
            todoList.addDeferred(new TodoId(String.valueOf(i)), tasks.get(i));
        }
        todoList.unlock();

        todoList.move(new TodoId("0"), new TodoId("2"));

        assertThat(todoList.read().getDeferredTodos()).extracting("task").containsExactly(
            "anotherTask",
            "yetAnotherTask",
            "someTask",
            "evenYetAnotherTask");
    }

    @Test
    public void move_whenTodoWithIdentifierExists_whenTargetExists_movesTodoUp() throws Exception {
        todoList.add(new TodoId("someId"), "now1");
        todoList.add(new TodoId("someId"), "now2");

        List<String> tasks = asList(
            "someTask",
            "anotherTask",
            "yetAnotherTask",
            "evenYetAnotherTask");

        for (int i = 0; i < tasks.size(); i++) {
            todoList.addDeferred(new TodoId(String.valueOf(i)), tasks.get(i));
        }
        todoList.unlock();

        todoList.move(new TodoId("3"), new TodoId("1"));

        assertThat(todoList.read().getDeferredTodos()).extracting("task").containsExactly(
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

        for (int i = 0; i < tasks.size(); i++) {
            todoList.addDeferred(new TodoId(String.valueOf(i)), tasks.get(i));
        }
        todoList.unlock();

        todoList.move(new TodoId("0"), new TodoId("0"));

        assertThat(todoList.read().getDeferredTodos()).extracting("task").isEqualTo(tasks);
    }

    @Test
    public void isAbleToBeUnlocked_whenThereAreNoListUnlocks_returnsTrue() throws Exception {
        Boolean ableToBeUnlocked = todoList.read().isAbleToBeUnlocked();

        assertThat(ableToBeUnlocked).isTrue();
    }

    @Test
    public void isAbleToBeUnlocked_whenThereAreListUnlocks_whenFirstListUnlockWasCreatedToday_returnsFalse() throws Exception {
        when(mockClock.instant()).thenReturn(Instant.ofEpochMilli(618537600000L)); // Tuesday, August 8, 1989 12:00:00 AM
        todoList.unlock();

        when(mockClock.instant()).thenReturn(Instant.ofEpochMilli(618623999999L)); // Tuesday, August 8, 1989 11:59:59 PM
        Boolean ableToBeUnlocked = todoList.read().isAbleToBeUnlocked();

        assertThat(ableToBeUnlocked).isFalse();
    }

    @Test
    public void isAbleToBeUnlocked_whenThereAreListUnlocks_whenFirstListUnlockWasCreatedBeforeToday_returnsTrue() throws Exception {
        when(mockClock.instant()).thenReturn(Instant.ofEpochMilli(618429599999L)); // Monday, August 7, 1989 11:29:59 PM
        todoList.unlock();

        when(mockClock.instant()).thenReturn(Instant.ofEpochMilli(618537600000L)); // Tuesday, August 8, 1989 12:00:00 AM
        Boolean ableToBeReplenished = todoList.read().isAbleToBeUnlocked();

        assertThat(ableToBeReplenished).isTrue();
    }

    @Test
    public void isAbleToBeUnlocked_whenThereAreListUnlocks_whenFirstListUnlockWasCreatedBeforeToday_butListIsStillUnlocked_returnsFalse() throws Exception {
        when(mockClock.instant()).thenReturn(Instant.ofEpochMilli(618536700000L)); // Monday, August 7, 1989 11:45:00 PM
        todoList.unlock();

        Instant currentInstant = Instant.ofEpochMilli(618537900000L); // Tuesday, August 8, 1989 12:05:00 AM
        when(mockClock.instant()).thenReturn(currentInstant);
        Boolean ableToBeReplenished = todoList.read().isAbleToBeUnlocked();

        assertThat(ableToBeReplenished).isFalse();
    }

    @Test
    public void unlock_whenListIsAbleToBeUnlocked_unlocksTheList() throws Exception {
        when(mockClock.instant()).thenReturn(Instant.now());

        assertThat(todoList.read().isAbleToBeUnlocked()).isTrue();

        todoList.unlock();

        assertThat(todoList.read().isAbleToBeUnlocked()).isFalse();
    }

    @Test
    public void unlock_whenListIsUnableToBeUnlocked_throwsLockTimerNotExpiredException() throws Exception {
        when(mockClock.instant()).thenReturn(Instant.now());
        todoList.unlock();

        exception.expect(LockTimerNotExpiredException.class);
        todoList.unlock();
    }

    @Test
    public void unlockDuration_whenListHasNeverBeenUnlocked_returns0() {
        assertThat(todoList.read().unlockDuration()).isEqualTo(0L);
    }

    @Test
    public void unlockDuration_whenListIsCurrentlyUnlocked_returnsRemainingDurationRelativeToNow_inMs() throws Exception {
        when(mockClock.instant()).thenReturn(Instant.ofEpochMilli(4900000).plus(Period.ofDays(1)));
        todoList.unlock();

        when(mockClock.instant()).thenReturn(Instant.ofEpochMilli(5000000L).plus(Period.ofDays(1)));
        assertThat(todoList.read().unlockDuration()).isEqualTo(1700000L);
    }

    @Test
    public void unlockDuration_whenListIsCurrentlyLocked_returns0() throws Exception {
        when(mockClock.instant()).thenReturn(Instant.ofEpochMilli(3000000L).plus(Period.ofDays(1)));
        todoList.unlock();

        when(mockClock.instant()).thenReturn(Instant.ofEpochMilli(5000000L).plus(Period.ofDays(1)));
        assertThat(todoList.read().unlockDuration()).isEqualTo(0L);
    }

    @Test
    public void pull_whenThereAreNoImmediateTodos_fillsFromPostponedList() throws Exception {
        todoList.addDeferred(new TodoId("someId"), "firstLater");
        todoList.addDeferred(new TodoId("someId"), "secondLater");

        todoList.pull();

        assertThat(todoList.read().getTodos()).extracting("task")
            .containsExactly("firstLater", "secondLater");
    }

    @Test
    public void pull_whenThereAreNoImmediateTodos_andOneDeferredTodo_fillsFromPostponedList() throws Exception {
        todoList.addDeferred(new TodoId("someId"), "firstLater");

        todoList.pull();

        assertThat(todoList.read().getTodos()).extracting("task")
            .containsExactly("firstLater");
    }

    @Test
    public void pull_whenThereAreLessImmediateTodosThanMaxSize_fillsFromPostponedList_withAsManyTodosAsTheDifference() throws Exception {
        todoList.add(new TodoId("someId"), "firstNow");
        todoList.addDeferred(new TodoId("someId"), "firstLater");
        todoList.addDeferred(new TodoId("someId"), "secondLater");
        todoList.addDeferred(new TodoId("someId"), "thirdLater");

        todoList.pull();

        assertThat(todoList.read().getTodos()).extracting("task")
            .containsExactly("firstNow", "firstLater");
    }

    @Test
    public void pull_whenThereAreAsManyImmediateTodosAsMaxSize_doesNotFillFromPostponedList() throws Exception {
        todoList.add(new TodoId("someId"), "someTask");
        todoList.add(new TodoId("someId"), "someOtherTask");
        todoList.addDeferred(new TodoId("someId"), "firstLater");

        todoList.pull();

        assertThat(todoList.read().getTodos()).extracting("task")
            .containsExactly("someOtherTask", "someTask");
    }

    @Test
    public void pull_whenThereIsASourceList_whenThereAreLessTodosThanMaxSize_whenSourceListIsEmpty_doesNotFillListFromSource() throws Exception {
        todoList.pull();

        assertThat(todoList.read().getTodos()).isEmpty();
    }

    @Test
    public void getTodos_getsTodosFromImmediateList() throws Exception {
        todoList.add(new TodoId("someId"), "someTask");

        assertThat(todoList.read().getTodos()).extracting("task")
            .contains("someTask");
    }

    @Test
    public void getTodos_doesNotGetTodosFromPostponedList() throws Exception {
        todoList.addDeferred(new TodoId("someId"), "someTask");

        assertThat(todoList.read().getTodos()).isEmpty();
    }

    @Test
    public void getDeferredTodos_getsTodosFromPostponedList() throws Exception {
        todoList.addDeferred(new TodoId("someId"), "someTask");

        todoList.unlock();
        assertThat(todoList.read().getDeferredTodos()).extracting("task")
            .contains("someTask");
    }

    @Test
    public void getDeferredTodos_doesNotGetTodosFromImmediateList() throws Exception {
        todoList.add(new TodoId("someId"), "someTask");

        todoList.unlock();
        assertThat(todoList.read().getDeferredTodos()).isEmpty();
    }

    @Test
    public void getDeferredTodos_whenListIsLocked_returnEmptyList() throws Exception {
        todoList.addDeferred(new TodoId("someId"), "someTask");

        List<Todo> deferredTodos = todoList.read().getDeferredTodos();

        assertThat(deferredTodos).isEmpty();
    }

    @Test
    public void getDeferredTodos_whenListIsNotLocked_getsTodosFromPostponedList() throws Exception {
        Instant unlockInstant = Instant.now();
        when(mockClock.instant()).thenReturn(unlockInstant);
        todoList.unlock();

        todoList.addDeferred(new TodoId("someId"), "someTask");

        when(mockClock.instant()).thenReturn(unlockInstant.plusSeconds(1799L));
        assertThat(todoList.read().getDeferredTodos()).extracting("task")
            .contains("someTask");
    }

    @Test
    public void isFull_whenCountOfTodos_isGreaterThanOrEqualToMaxSize_returnsTrue() throws Exception {
        todoList.add(new TodoId("someId"), "todo1");
        todoList.add(new TodoId("someId"), "todo2");

        assertThat(todoList.read().isFull()).isTrue();
    }

    @Test
    public void isFull_whenCountOfTodos_isLessThanMaxSize_returnsFalse() throws Exception {
        assertThat(todoList.read().isFull()).isEqualTo(false);
    }

    @Test
    public void isAbleToBeReplenished_whenThereAreDeferredTodos_andTheListIsNotFull_returnsTrue() throws Exception {
        todoList.addDeferred(new TodoId("someId"), "someTask");

        boolean hasDeferredTodosAvailable = todoList.read().isAbleToBeReplenished();

        assertThat(hasDeferredTodosAvailable).isTrue();
    }

    @Test
    public void isAbleToBeReplenished_whenThereAreDeferredTodos_andTheListIsFull_returnsFalse() throws Exception {
        todoList.add(new TodoId("someId"), "todo1");
        todoList.add(new TodoId("someId"), "todo2");
        todoList.addDeferred(new TodoId("someId"), "someTask");

        boolean hasDeferredTodosAvailable = todoList.read().isAbleToBeReplenished();

        assertThat(hasDeferredTodosAvailable).isFalse();
    }

    @Test
    public void isAbleToBeReplenished_whenThereAreNoDeferredTodos_andTheListIsNotFull_returnsFalse() throws Exception {
        todoList.add(new TodoId("someId"), "todo1");

        boolean hasDeferredTodosAvailable = todoList.read().isAbleToBeReplenished();

        assertThat(hasDeferredTodosAvailable).isFalse();
    }
}