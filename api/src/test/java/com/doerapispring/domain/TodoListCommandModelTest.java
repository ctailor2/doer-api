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

public class TodoListCommandModelTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();
    private TodoListCommandModel todoListCommandModel;
    private Clock mockClock;
    private UserId userId;
    private ListId listId;

    @Before
    public void setUp() throws Exception {
        mockClock = mock(Clock.class);

        when(mockClock.getZone()).thenReturn(ZoneId.of("UTC"));
        when(mockClock.instant()).thenAnswer(invocation -> Instant.now());

        userId = new UserId("something");
        listId = new ListId("someListId");
        todoListCommandModel = new TodoListCommandModel(
            mockClock,
            userId,
            listId,
            "someName",
            Date.from(Instant.EPOCH),
            new ArrayList<>(),
            0);
    }

    @Test
    public void add_addsToNowList() throws Exception {
        todoListCommandModel.add(new TodoId("someId"), "someTask");

        assertThat(todoListCommandModel.read().getTodos()).containsExactly(new Todo(new TodoId("someId"), "someTask"));
    }

    @Test
    public void add_addsToNowList_beforeFirstTodo() throws Exception {
        todoListCommandModel.add(new TodoId("someId"), "someTask");
        todoListCommandModel.add(new TodoId("someId"), "someOtherTask");

        assertThat(todoListCommandModel.read().getTodos()).extracting("task")
            .containsExactly("someOtherTask", "someTask");
    }

    @Test
    public void add_whenListAlreadyContainsTask_doesNotAdd_throwsDuplicateTodoException() throws Exception {
        todoListCommandModel.add(new TodoId("someId"), "sameTask");

        exception.expect(DuplicateTodoException.class);
        todoListCommandModel.add(new TodoId("someId"), "sameTask");
    }

    @Test
    public void add_whenListIsFull_doesNotAdd_throwsListSizeExceededException() throws Exception {
        todoListCommandModel.add(new TodoId("someId"), "someTask");
        todoListCommandModel.add(new TodoId("someId"), "someOtherTask");

        exception.expect(ListSizeExceededException.class);
        todoListCommandModel.add(new TodoId("someId"), "stillAnotherTask");
    }

    @Test
    public void addDeferred_addsToLaterList() throws Exception {
        todoListCommandModel.addDeferred(new TodoId("someId"), "someTask");

        todoListCommandModel.unlock();
        assertThat(todoListCommandModel.read().getDeferredTodos()).containsExactly(new Todo(new TodoId("someId"), "someTask"));
    }

    @Test
    public void addDeferred_addsToLaterList_afterLastTodo() throws Exception {
        todoListCommandModel.addDeferred(new TodoId("someId"), "someTask");
        todoListCommandModel.addDeferred(new TodoId("someId"), "someOtherTask");

        todoListCommandModel.unlock();
        assertThat(todoListCommandModel.read().getDeferredTodos()).extracting("task")
            .containsExactly("someTask", "someOtherTask");
    }

    @Test
    public void addDeferred_whenListAlreadyContainsTask_doesNotAdd_throwsDuplicateTodoException() throws Exception {
        todoListCommandModel.addDeferred(new TodoId("someId"), "sameTask");

        exception.expect(DuplicateTodoException.class);
        todoListCommandModel.addDeferred(new TodoId("someId"), "sameTask");
    }

    @Test
    public void delete_whenTodoWithIdentifierExists_removesTodo() throws Exception {
        TodoId todoId = new TodoId("someId");
        todoListCommandModel.add(todoId, "someTask");

        todoListCommandModel.delete(todoId);

        assertThat(todoListCommandModel.read().getTodos()).isEmpty();
    }

    @Test
    public void delete_whenTodoWithIdentifierExists_removesDeferredTodo() throws Exception {
        TodoId todoId = new TodoId("someId");
        todoListCommandModel.addDeferred(todoId, "someTask");
        todoListCommandModel.unlock();

        todoListCommandModel.delete(todoId);

        assertThat(todoListCommandModel.read().getDeferredTodos()).isEmpty();
    }

    @Test
    public void delete_whenTodoWithIdentifierDoesNotExist_throwsNotFoundException() throws Exception {
        exception.expect(TodoNotFoundException.class);
        todoListCommandModel.delete(new TodoId("someBogusIdentifier"));
    }

    @Test
    public void displace_whenListIsNotFull_throwsListNotFullException() throws Exception {
        exception.expect(ListNotFullException.class);
        todoListCommandModel.displace(null, "someTask");
    }

    @Test
    public void displace_whenListIsFull_whenTaskAlreadyExists_throwsDuplicateTodoException() throws Exception {
        todoListCommandModel.add(new TodoId("someId"), "task");
        todoListCommandModel.add(new TodoId("someId"), "sameTask");

        exception.expect(DuplicateTodoException.class);
        todoListCommandModel.displace(new TodoId("someId"), "sameTask");
    }

    @Test
    public void displace_whenPostponedListIsEmpty_replacesTodo_andPushesItIntoPostponedListWithCorrectPositioning() throws Exception {
        todoListCommandModel.add(new TodoId("1"), "someNowTask");
        todoListCommandModel.add(new TodoId("2"), "someOtherNowTask");

        todoListCommandModel.displace(new TodoId("3"), "displace it");

        assertThat(todoListCommandModel.read().getTodos()).containsExactly(
            new Todo(new TodoId("3"), "displace it"),
            new Todo(new TodoId("2"), "someOtherNowTask"));
        todoListCommandModel.unlock();
        assertThat(todoListCommandModel.read().getDeferredTodos()).containsExactly(
            new Todo(new TodoId("1"), "someNowTask"));
    }

    @Test
    public void displace_whenPostponedIsNotEmpty_replacesTodo_andPushesItIntoPostponedListWithCorrectPositioning() throws Exception {
        todoListCommandModel.add(new TodoId("1"), "someNowTask");
        todoListCommandModel.add(new TodoId("2"), "someOtherNowTask");
        todoListCommandModel.addDeferred(new TodoId("3"), "someLaterTask");

        todoListCommandModel.displace(new TodoId("4"), "displace it");

        assertThat(todoListCommandModel.read().getTodos()).containsExactly(
            new Todo(new TodoId("4"), "displace it"),
            new Todo(new TodoId("2"), "someOtherNowTask"));
        todoListCommandModel.unlock();
        assertThat(todoListCommandModel.read().getDeferredTodos()).containsExactly(
            new Todo(new TodoId("1"), "someNowTask"),
            new Todo(new TodoId("3"), "someLaterTask"));
    }

    @Test
    public void update_whenTodoWithIdentifierExists_updatesTodo() throws Exception {
        TodoId todoId = new TodoId("someId");
        todoListCommandModel.add(todoId, "someTask");

        todoListCommandModel.update(todoId, "someOtherTask");

        Todo todo = todoListCommandModel.read().getTodos().get(0);
        assertThat(todo.getTask()).isEqualTo("someOtherTask");
    }

    @Test
    public void update_whenTaskAlreadyExists_throwsDuplicateTodoException() throws Exception {
        TodoId todoId = new TodoId("someId");
        todoListCommandModel.add(todoId, "sameTask");

        exception.expect(DuplicateTodoException.class);
        todoListCommandModel.update(todoId, "sameTask");
    }

    @Test
    public void update_whenTodoWithIdentifierDoesNotExist_throwsNotFoundException() throws Exception {
        exception.expect(TodoNotFoundException.class);
        todoListCommandModel.update(new TodoId("bananaPudding"), "sameTask");
    }

    @Test
    public void complete_whenTodoWithIdentifierExists_removesTodoFromMatchingList_returnsCompletedTodo() throws Exception {
        Instant now = Instant.now();
        when(mockClock.instant()).thenReturn(now);

        TodoId todoId = new TodoId("someId");
        todoListCommandModel.add(todoId, "someTask");

        todoListCommandModel.complete(todoId);

        assertThat(todoListCommandModel.read().getTodos()).isEmpty();
    }

    @Test
    public void complete_addsTodoCompletedEvent() throws Exception {
        Instant now = Instant.now();
        when(mockClock.instant()).thenReturn(now);

        TodoId todoId = new TodoId("someId");
        todoListCommandModel.add(todoId, "someTask");

        todoListCommandModel.complete(todoId);
        List<DomainEvent> domainEvents = todoListCommandModel.getDomainEvents();

        assertThat(todoListCommandModel.read().getTodos()).isEmpty();
        assertThat(domainEvents).contains(new TodoCompletedEvent(
            userId,
            listId,
            new CompletedTodoId("someId"),
            "someTask",
            Date.from(now)));
    }

    @Test
    public void complete_whenTodoWithIdentifierDoesNotExist_throwsNotFoundException() throws Exception {
        exception.expect(TodoNotFoundException.class);
        todoListCommandModel.complete(new TodoId("someBogusIdentifier"));
    }

    @Test
    public void move_whenTodoWithIdentifierDoesNotExist_throwsNotFoundException() throws Exception {
        exception.expect(TodoNotFoundException.class);
        todoListCommandModel.move(new TodoId("junk"), new TodoId("bogus"));
    }

    @Test
    public void move_whenTodoWithIdentifierExists_whenTargetExists_movesTodoDown() throws Exception {
        todoListCommandModel.add(new TodoId("someId"), "now1");
        todoListCommandModel.add(new TodoId("someId"), "now2");

        List<String> tasks = Arrays.asList(
            "someTask",
            "anotherTask",
            "yetAnotherTask",
            "evenYetAnotherTask");

        for (int i = 0; i < tasks.size(); i++) {
            todoListCommandModel.addDeferred(new TodoId(String.valueOf(i)), tasks.get(i));
        }
        todoListCommandModel.unlock();

        todoListCommandModel.move(new TodoId("0"), new TodoId("2"));

        assertThat(todoListCommandModel.read().getDeferredTodos()).extracting("task").containsExactly(
            "anotherTask",
            "yetAnotherTask",
            "someTask",
            "evenYetAnotherTask");
    }

    @Test
    public void move_whenTodoWithIdentifierExists_whenTargetExists_movesTodoUp() throws Exception {
        todoListCommandModel.add(new TodoId("someId"), "now1");
        todoListCommandModel.add(new TodoId("someId"), "now2");

        List<String> tasks = asList(
            "someTask",
            "anotherTask",
            "yetAnotherTask",
            "evenYetAnotherTask");

        for (int i = 0; i < tasks.size(); i++) {
            todoListCommandModel.addDeferred(new TodoId(String.valueOf(i)), tasks.get(i));
        }
        todoListCommandModel.unlock();

        todoListCommandModel.move(new TodoId("3"), new TodoId("1"));

        assertThat(todoListCommandModel.read().getDeferredTodos()).extracting("task").containsExactly(
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
            todoListCommandModel.addDeferred(new TodoId(String.valueOf(i)), tasks.get(i));
        }
        todoListCommandModel.unlock();

        todoListCommandModel.move(new TodoId("0"), new TodoId("0"));

        assertThat(todoListCommandModel.read().getDeferredTodos()).extracting("task").isEqualTo(tasks);
    }

    @Test
    public void isAbleToBeUnlocked_whenThereAreNoListUnlocks_returnsTrue() throws Exception {
        Boolean ableToBeUnlocked = todoListCommandModel.read().isAbleToBeUnlocked();

        assertThat(ableToBeUnlocked).isTrue();
    }

    @Test
    public void isAbleToBeUnlocked_whenThereAreListUnlocks_whenFirstListUnlockWasCreatedToday_returnsFalse() throws Exception {
        when(mockClock.instant()).thenReturn(Instant.ofEpochMilli(618537600000L)); // Tuesday, August 8, 1989 12:00:00 AM
        todoListCommandModel.unlock();

        when(mockClock.instant()).thenReturn(Instant.ofEpochMilli(618623999999L)); // Tuesday, August 8, 1989 11:59:59 PM
        Boolean ableToBeUnlocked = todoListCommandModel.read().isAbleToBeUnlocked();

        assertThat(ableToBeUnlocked).isFalse();
    }

    @Test
    public void isAbleToBeUnlocked_whenThereAreListUnlocks_whenFirstListUnlockWasCreatedBeforeToday_returnsTrue() throws Exception {
        when(mockClock.instant()).thenReturn(Instant.ofEpochMilli(618429599999L)); // Monday, August 7, 1989 11:29:59 PM
        todoListCommandModel.unlock();

        when(mockClock.instant()).thenReturn(Instant.ofEpochMilli(618537600000L)); // Tuesday, August 8, 1989 12:00:00 AM
        Boolean ableToBeReplenished = todoListCommandModel.read().isAbleToBeUnlocked();

        assertThat(ableToBeReplenished).isTrue();
    }

    @Test
    public void isAbleToBeUnlocked_whenThereAreListUnlocks_whenFirstListUnlockWasCreatedBeforeToday_butListIsStillUnlocked_returnsFalse() throws Exception {
        when(mockClock.instant()).thenReturn(Instant.ofEpochMilli(618536700000L)); // Monday, August 7, 1989 11:45:00 PM
        todoListCommandModel.unlock();

        Instant currentInstant = Instant.ofEpochMilli(618537900000L); // Tuesday, August 8, 1989 12:05:00 AM
        when(mockClock.instant()).thenReturn(currentInstant);
        Boolean ableToBeReplenished = todoListCommandModel.read().isAbleToBeUnlocked();

        assertThat(ableToBeReplenished).isFalse();
    }

    @Test
    public void unlock_whenListIsAbleToBeUnlocked_unlocksTheList() throws Exception {
        when(mockClock.instant()).thenReturn(Instant.now());

        assertThat(todoListCommandModel.read().isAbleToBeUnlocked()).isTrue();

        todoListCommandModel.unlock();

        assertThat(todoListCommandModel.read().isAbleToBeUnlocked()).isFalse();
    }

    @Test
    public void unlock_whenListIsUnableToBeUnlocked_throwsLockTimerNotExpiredException() throws Exception {
        when(mockClock.instant()).thenReturn(Instant.now());
        todoListCommandModel.unlock();

        exception.expect(LockTimerNotExpiredException.class);
        todoListCommandModel.unlock();
    }

    @Test
    public void unlockDuration_whenListHasNeverBeenUnlocked_returns0() {
        assertThat(todoListCommandModel.read().unlockDuration()).isEqualTo(0L);
    }

    @Test
    public void unlockDuration_whenListIsCurrentlyUnlocked_returnsRemainingDurationRelativeToNow_inMs() throws Exception {
        when(mockClock.instant()).thenReturn(Instant.ofEpochMilli(4900000).plus(Period.ofDays(1)));
        todoListCommandModel.unlock();

        when(mockClock.instant()).thenReturn(Instant.ofEpochMilli(5000000L).plus(Period.ofDays(1)));
        assertThat(todoListCommandModel.read().unlockDuration()).isEqualTo(1700000L);
    }

    @Test
    public void unlockDuration_whenListIsCurrentlyLocked_returns0() throws Exception {
        when(mockClock.instant()).thenReturn(Instant.ofEpochMilli(3000000L).plus(Period.ofDays(1)));
        todoListCommandModel.unlock();

        when(mockClock.instant()).thenReturn(Instant.ofEpochMilli(5000000L).plus(Period.ofDays(1)));
        assertThat(todoListCommandModel.read().unlockDuration()).isEqualTo(0L);
    }

    @Test
    public void pull_whenThereAreNoImmediateTodos_fillsFromPostponedList() throws Exception {
        todoListCommandModel.addDeferred(new TodoId("someId"), "firstLater");
        todoListCommandModel.addDeferred(new TodoId("someId"), "secondLater");

        todoListCommandModel.pull();

        assertThat(todoListCommandModel.read().getTodos()).extracting("task")
            .containsExactly("firstLater", "secondLater");
    }

    @Test
    public void pull_whenThereAreNoImmediateTodos_andOneDeferredTodo_fillsFromPostponedList() throws Exception {
        todoListCommandModel.addDeferred(new TodoId("someId"), "firstLater");

        todoListCommandModel.pull();

        assertThat(todoListCommandModel.read().getTodos()).extracting("task")
            .containsExactly("firstLater");
    }

    @Test
    public void pull_whenThereAreLessImmediateTodosThanMaxSize_fillsFromPostponedList_withAsManyTodosAsTheDifference() throws Exception {
        todoListCommandModel.add(new TodoId("someId"), "firstNow");
        todoListCommandModel.addDeferred(new TodoId("someId"), "firstLater");
        todoListCommandModel.addDeferred(new TodoId("someId"), "secondLater");
        todoListCommandModel.addDeferred(new TodoId("someId"), "thirdLater");

        todoListCommandModel.pull();

        assertThat(todoListCommandModel.read().getTodos()).extracting("task")
            .containsExactly("firstNow", "firstLater");
    }

    @Test
    public void pull_whenThereAreAsManyImmediateTodosAsMaxSize_doesNotFillFromPostponedList() throws Exception {
        todoListCommandModel.add(new TodoId("someId"), "someTask");
        todoListCommandModel.add(new TodoId("someId"), "someOtherTask");
        todoListCommandModel.addDeferred(new TodoId("someId"), "firstLater");

        todoListCommandModel.pull();

        assertThat(todoListCommandModel.read().getTodos()).extracting("task")
            .containsExactly("someOtherTask", "someTask");
    }

    @Test
    public void pull_whenThereIsASourceList_whenThereAreLessTodosThanMaxSize_whenSourceListIsEmpty_doesNotFillListFromSource() throws Exception {
        todoListCommandModel.pull();

        assertThat(todoListCommandModel.read().getTodos()).isEmpty();
    }

    @Test
    public void getTodos_getsTodosFromImmediateList() throws Exception {
        todoListCommandModel.add(new TodoId("someId"), "someTask");

        assertThat(todoListCommandModel.read().getTodos()).extracting("task")
            .contains("someTask");
    }

    @Test
    public void getTodos_doesNotGetTodosFromPostponedList() throws Exception {
        todoListCommandModel.addDeferred(new TodoId("someId"), "someTask");

        assertThat(todoListCommandModel.read().getTodos()).isEmpty();
    }

    @Test
    public void getDeferredTodos_getsTodosFromPostponedList() throws Exception {
        todoListCommandModel.addDeferred(new TodoId("someId"), "someTask");

        todoListCommandModel.unlock();
        assertThat(todoListCommandModel.read().getDeferredTodos()).extracting("task")
            .contains("someTask");
    }

    @Test
    public void getDeferredTodos_doesNotGetTodosFromImmediateList() throws Exception {
        todoListCommandModel.add(new TodoId("someId"), "someTask");

        todoListCommandModel.unlock();
        assertThat(todoListCommandModel.read().getDeferredTodos()).isEmpty();
    }

    @Test
    public void getDeferredTodos_whenListIsLocked_returnEmptyList() throws Exception {
        todoListCommandModel.addDeferred(new TodoId("someId"), "someTask");

        List<Todo> deferredTodos = todoListCommandModel.read().getDeferredTodos();

        assertThat(deferredTodos).isEmpty();
    }

    @Test
    public void getDeferredTodos_whenListIsNotLocked_getsTodosFromPostponedList() throws Exception {
        Instant unlockInstant = Instant.now();
        when(mockClock.instant()).thenReturn(unlockInstant);
        todoListCommandModel.unlock();

        todoListCommandModel.addDeferred(new TodoId("someId"), "someTask");

        when(mockClock.instant()).thenReturn(unlockInstant.plusSeconds(1799L));
        assertThat(todoListCommandModel.read().getDeferredTodos()).extracting("task")
            .contains("someTask");
    }

    @Test
    public void isFull_whenCountOfTodos_isGreaterThanOrEqualToMaxSize_returnsTrue() throws Exception {
        todoListCommandModel.add(new TodoId("someId"), "todo1");
        todoListCommandModel.add(new TodoId("someId"), "todo2");

        assertThat(todoListCommandModel.read().isFull()).isTrue();
    }

    @Test
    public void isFull_whenCountOfTodos_isLessThanMaxSize_returnsFalse() throws Exception {
        assertThat(todoListCommandModel.read().isFull()).isEqualTo(false);
    }

    @Test
    public void isAbleToBeReplenished_whenThereAreDeferredTodos_andTheListIsNotFull_returnsTrue() throws Exception {
        todoListCommandModel.addDeferred(new TodoId("someId"), "someTask");

        boolean hasDeferredTodosAvailable = todoListCommandModel.read().isAbleToBeReplenished();

        assertThat(hasDeferredTodosAvailable).isTrue();
    }

    @Test
    public void isAbleToBeReplenished_whenThereAreDeferredTodos_andTheListIsFull_returnsFalse() throws Exception {
        todoListCommandModel.add(new TodoId("someId"), "todo1");
        todoListCommandModel.add(new TodoId("someId"), "todo2");
        todoListCommandModel.addDeferred(new TodoId("someId"), "someTask");

        boolean hasDeferredTodosAvailable = todoListCommandModel.read().isAbleToBeReplenished();

        assertThat(hasDeferredTodosAvailable).isFalse();
    }

    @Test
    public void isAbleToBeReplenished_whenThereAreNoDeferredTodos_andTheListIsNotFull_returnsFalse() throws Exception {
        todoListCommandModel.add(new TodoId("someId"), "todo1");

        boolean hasDeferredTodosAvailable = todoListCommandModel.read().isAbleToBeReplenished();

        assertThat(hasDeferredTodosAvailable).isFalse();
    }

    @Test
    public void escalate_swapsPositionsOfLastTodoAndFirstDeferredTodo() throws Exception {
        todoListCommandModel.add(new TodoId("someId"), "will be deferred after escalate");
        todoListCommandModel.add(new TodoId("someId"), "some task");
        todoListCommandModel.addDeferred(new TodoId("someId"), "will no longer be deferred after escalate");

        todoListCommandModel.escalate();
        todoListCommandModel.unlock();

        TodoListReadModel todoListReadModel = todoListCommandModel.read();
        assertThat(todoListReadModel.getTodos()).containsExactly(
            new Todo(new TodoId("someId"), "some task"),
            new Todo(new TodoId("someId"), "will no longer be deferred after escalate"));
        assertThat(todoListReadModel.getDeferredTodos()).containsExactly(
            new Todo(new TodoId("someId"), "will be deferred after escalate"));
    }

    @Test
    public void escalate_whenListIsNotAbleToBeEscalated_throwsEscalateNotAllowedException() throws Exception {
        todoListCommandModel.add(new TodoId("someId"), "task 2");

        exception.expect(EscalateNotAllowException.class);
        todoListCommandModel.escalate();
    }

    @Test
    public void isAbleToBeEscalated_whenTheListIsFull_andThereAreDeferredTodos_returnsTrue() throws Exception {
        todoListCommandModel.add(new TodoId("someId"), "task 1");
        todoListCommandModel.add(new TodoId("someId"), "task 2");
        todoListCommandModel.addDeferred(new TodoId("someId"), "task 3");

        assertThat(todoListCommandModel.read().isAbleToBeEscalated()).isTrue();
    }

    @Test
    public void isAbleToBeEscalated_whenTheListIsNotFull_andThereAreDeferredTodos_returnsFalse() throws Exception {
        todoListCommandModel.add(new TodoId("someId"), "task 1");
        todoListCommandModel.addDeferred(new TodoId("someId"), "task 2");

        assertThat(todoListCommandModel.read().isAbleToBeEscalated()).isFalse();
    }

    @Test
    public void isAbleToBeEscalated_whenTheListIsFull_andThereAreNoDeferredTodos_returnsFalse() throws Exception {
        todoListCommandModel.add(new TodoId("someId"), "task 1");
        todoListCommandModel.add(new TodoId("someId"), "task 2");

        assertThat(todoListCommandModel.read().isAbleToBeEscalated()).isFalse();
    }
}