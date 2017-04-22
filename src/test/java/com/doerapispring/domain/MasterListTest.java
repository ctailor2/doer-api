package com.doerapispring.domain;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;


public class MasterListTest {
    private MasterList masterList;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        masterList = new MasterList(new UniqueIdentifier("something"), 2, Collections.emptyList());
    }

    @Test
    public void add_immediateTodo_toEmptyList_addsToList_returnsTodoWithCorrectPosition() throws Exception {
        Todo firstTodo = masterList.add("someTask", ScheduledFor.now);
        assertThat(masterList.getTodos()).containsExactly(firstTodo);
        assertThat(firstTodo.getPosition()).isEqualTo(1);
    }

    @Test
    public void add_immediateTodo_toListWithImmediateTodos_addsTodoAfterLast() throws Exception {
        Todo firstTodo = masterList.add("someTask", ScheduledFor.now);
        Todo secondTodo = masterList.add("someOtherTask", ScheduledFor.now);

        assertThat(masterList.getTodos()).containsExactly(firstTodo, secondTodo);
        assertThat(secondTodo.getPosition()).isEqualTo(2);
    }

    @Test
    public void add_postponedTodo_toEmptyList_addsToList_returnsTodoWithCorrectPosition() throws Exception {
        Todo firstTodo = masterList.add("someTask", ScheduledFor.later);
        assertThat(masterList.getTodos()).containsExactly(firstTodo);
        assertThat(firstTodo.getPosition()).isEqualTo(1);
    }

    @Test
    public void add_postponedTodo_toListWithPostponedTodos_addsTodoAfterLast() throws Exception {
        Todo firstTodo = masterList.add("someTask", ScheduledFor.later);
        Todo secondTodo = masterList.add("someOtherTask", ScheduledFor.later);

        assertThat(masterList.getTodos()).containsExactly(firstTodo, secondTodo);
        assertThat(secondTodo.getPosition()).isEqualTo(2);
    }

    @Test
    public void add_whenListAlreadyContainsTask_doesNotAdd_throwsDuplicateTodoException() throws Exception {
        masterList.add("someTask", ScheduledFor.now);

        exception.expect(DuplicateTodoException.class);
        masterList.add("someTask", ScheduledFor.later);
    }

    @Test
    public void add_whenListIsFull_doesNotAdd_throwsListSizeExceededException() throws Exception {
        masterList.add("someTask", ScheduledFor.now);
        masterList.add("someOtherTask", ScheduledFor.now);

        exception.expect(ListSizeExceededException.class);
        masterList.add("stillAnotherTask", ScheduledFor.now);
    }

    @Test
    public void delete_whenTodoWithIdentifierExists_removesTodo() throws Exception {
        Todo todo = masterList.add("someTask", ScheduledFor.now);

        Todo deletedTodo = masterList.delete(todo.getLocalIdentifier());

        assertThat(deletedTodo).isEqualTo(todo);
        assertThat(masterList.getTodos()).doesNotContain(todo);
    }

    @Test
    public void delete_whenTodoWithIdentifierDoesNotExist_throwsNotFoundException() throws Exception {
        exception.expect(TodoNotFoundException.class);
        masterList.delete("someBogusIdentifier");
    }

    @Test
    public void displace_whenTodoWithIdentifierExists_inImmediateList_displacesIt_correctlyPositionsWhenNoPostponedTodos() throws Exception {
        Todo todo = masterList.add("someTask", ScheduledFor.now);

        List<Todo> todos = masterList.displace(todo.getLocalIdentifier(), "displace it");

        Todo displacedTodo = new Todo("someTask", ScheduledFor.later, 1);
        Todo newTodo = new Todo("displace it", ScheduledFor.now, todo.getPosition());
        assertThat(todos).contains(displacedTodo, newTodo);
        assertThat(masterList.getTodos()).contains(newTodo, displacedTodo);
    }

    @Test
    public void displace_whenTodoWithIdentifierExists_inImmediateList_displacesIt_correctlyPositionsWhenThereArePostponedTodos() throws Exception {
        Todo todo = masterList.add("someTask", ScheduledFor.now);
        Todo postponedTodo = masterList.add("someOtherTask", ScheduledFor.later);

        List<Todo> todos = masterList.displace(todo.getLocalIdentifier(), "displace it");

        Todo displacedTodo = new Todo("someTask", ScheduledFor.later, 0);
        Todo newTodo = new Todo("displace it", ScheduledFor.now, todo.getPosition());
        assertThat(todos).contains(displacedTodo, newTodo);
        assertThat(masterList.getTodos()).contains(newTodo, displacedTodo, postponedTodo);
    }

    @Test
    public void displace_whenTodoWithIdentifierDoesNotExist_throwsNotFoundException() throws Exception {
        exception.expect(TodoNotFoundException.class);
        masterList.displace("someId", "displace it");
    }

    @Test
    public void displace_whenTaskAlreadyExists_throwsDuplicateTodoException() throws Exception {
        Todo todo = masterList.add("sameTask", ScheduledFor.now);

        exception.expect(DuplicateTodoException.class);
        masterList.displace(todo.getLocalIdentifier(), "sameTask");
    }

    @Test
    public void update_whenTodoWithIdentifierExists_updatesTodo() throws Exception {
        Todo todo = masterList.add("someTask", ScheduledFor.now);

        Todo updatedTodo = masterList.update(todo.getLocalIdentifier(), "someOtherTask");

        assertThat(updatedTodo).isEqualTo(new Todo("someOtherTask", todo.getScheduling(), todo.getPosition()));
        assertThat(masterList.getTodos()).containsOnly(updatedTodo);
    }

    @Test
    public void update_whenTaskAlreadyExists_throwsDuplicateTodoException() throws Exception {
        Todo todo = masterList.add("sameTask", ScheduledFor.now);

        exception.expect(DuplicateTodoException.class);
        masterList.update(todo.getLocalIdentifier(), "sameTask");
    }

    @Test
    public void update_whenTodoWithIdentifierDoesNotExist_throwsNotFoundException() throws Exception {
        exception.expect(TodoNotFoundException.class);
        masterList.update("bananaPudding", "sameTask");
    }

    @Test
    public void complete_whenTodoWithIdentifierExists_removesTodo() throws Exception {
        Todo todo = masterList.add("someTask", ScheduledFor.now);

        Todo completedTodo = masterList.complete(todo.getLocalIdentifier());

        assertThat(completedTodo.isComplete()).isEqualTo(true);
        assertThat(masterList.getTodos()).doesNotContain(todo);
    }

    @Test
    public void complete_whenTodoWithIdentifierDoesNotExist_throwsNotFoundException() throws Exception {
        exception.expect(TodoNotFoundException.class);
        masterList.complete("someBogusIdentifier");
    }

    @Test
    public void move_whenTodoWithIdentifierExists_whenTargetDoesNotExist_throwsNotFoundException() throws Exception {
        Todo todo = masterList.add("someTask", ScheduledFor.later);

        exception.expect(TodoNotFoundException.class);
        masterList.move(todo.getLocalIdentifier(), "nonExistentIdentifier");
    }

    @Test
    public void move_whenTodoWithIdentifierDoesNotExist_throwsNotFoundException() throws Exception {
        exception.expect(TodoNotFoundException.class);
        masterList.move("junk", "bogus");
    }

    @Test
    public void move_whenTodoWithIdentifierExists_whenTargetDoesNotExist_inMatchingList_throwsNotFoundException() throws Exception {
        Todo laterTodo = masterList.add("someTask", ScheduledFor.later);
        masterList.add("identifierFromDifferentList", ScheduledFor.now);

        exception.expect(TodoNotFoundException.class);
        masterList.move(laterTodo.getLocalIdentifier(), "identifierFromDifferentList");
    }

    @Test
    public void move_whenTodoWithIdentifierExists_whenTargetExists_movesTodoDown() throws Exception {
        Todo fourthTodo = new Todo("D", "evenYetAnotherTask", ScheduledFor.later, 4);
        List<Todo> todos = asList(
                new Todo("A", "someTask", ScheduledFor.later, 1),
                new Todo("B", "anotherTask", ScheduledFor.later, 2),
                new Todo("C", "yetAnotherTask", ScheduledFor.later, 3),
                fourthTodo);

        MasterList masterList = new MasterList(new UniqueIdentifier("something"), 2, todos);

        List<Todo> effectedTodos = masterList.move("A", "C");

        Todo expectedFirstTodo = new Todo("B", "anotherTask", ScheduledFor.later, 1);
        Todo expectedSecondTodo = new Todo("C", "yetAnotherTask", ScheduledFor.later, 2);
        Todo expectedThirdTodo = new Todo("A", "someTask", ScheduledFor.later, 3);

        assertThat(effectedTodos).contains(expectedFirstTodo, expectedSecondTodo, expectedThirdTodo);
        assertThat(masterList.getTodos()).containsExactly(
                expectedFirstTodo,
                expectedSecondTodo,
                expectedThirdTodo,
                fourthTodo);
    }

    @Test
    public void move_whenTodoWithIdentifierExists_whenTargetExists_movesTodoUp() throws Exception {
        Todo firstTodo = new Todo("A", "someTask", ScheduledFor.now, 1);
        List<Todo> todos = asList(firstTodo,
                new Todo("B", "anotherTask", ScheduledFor.now, 2),
                new Todo("C", "yetAnotherTask", ScheduledFor.now, 3),
                new Todo("D", "evenYetAnotherTask", ScheduledFor.now, 4));

        MasterList masterList = new MasterList(new UniqueIdentifier("something"), 2, todos);

        List<Todo> effectedTodos = masterList.move("D", "B");

        Todo expectedSecondTodo = new Todo("D", "evenYetAnotherTask", ScheduledFor.now, 2);
        Todo expectedThirdTodo = new Todo("B", "anotherTask", ScheduledFor.now, 3);
        Todo expectedFourthTodo = new Todo("C", "yetAnotherTask", ScheduledFor.now, 4);

        assertThat(effectedTodos).contains(expectedSecondTodo, expectedThirdTodo, expectedFourthTodo);
        assertThat(masterList.getTodos()).containsExactly(
                firstTodo,
                expectedSecondTodo,
                expectedThirdTodo,
                expectedFourthTodo);
    }

    @Test
    public void move_beforeOrAfter_whenTodoWithIdentifierExists_whenTargetExists_whenOriginalAndTargetPositionsAreSame_doesNothing() throws Exception {
        List<Todo> todos = asList(
                new Todo("A", "someTask", ScheduledFor.later, 1),
                new Todo("B", "anotherTask", ScheduledFor.later, 2),
                new Todo("C", "yetAnotherTask", ScheduledFor.later, 3),
                new Todo("D", "evenYetAnotherTask", ScheduledFor.later, 4));

        MasterList masterList = new MasterList(new UniqueIdentifier("something"), 2, todos);

        List<Todo> effectedTodos = masterList.move("A", "A");

        assertThat(effectedTodos).isEmpty();
        assertThat(masterList.getTodos()).isEqualTo(todos);
    }

    @Test
    public void pull_whenThereAreNoNowTodos_returnsLaterTodos() throws Exception {
        List<Todo> todos = asList(
                new Todo("A", "firstLater", ScheduledFor.later, 1),
                new Todo("B", "secondLater", ScheduledFor.later, 2));
        MasterList masterList = new MasterList(new UniqueIdentifier("something"), 2, todos);

        List<Todo> effectedTodos = masterList.pull();

        assertThat(effectedTodos).contains(
                new Todo("A", "firstLater", ScheduledFor.now, 1),
                new Todo("B", "secondLater", ScheduledFor.now, 2));
        assertThat(masterList.getTodos()).isEqualTo(effectedTodos);
    }

    @Test
    public void pull_whenThereAreLessNowTodosThanFocusSize_returnsAsManyLaterTodosAsTheDifference() throws Exception {
        List<Todo> todos = asList(
                new Todo("A", "onlyNow", ScheduledFor.now, 1),
                new Todo("B", "firstLater", ScheduledFor.later, 1),
                new Todo("C", "secondLater", ScheduledFor.later, 2));
        MasterList masterList = new MasterList(new UniqueIdentifier("something"), 2, todos);

        List<Todo> effectedTodos = masterList.pull();

        assertThat(effectedTodos).contains(new Todo("B", "firstLater", ScheduledFor.now, 2));
        assertThat(masterList.getTodos()).containsExactly(
                new Todo("A", "onlyNow", ScheduledFor.now, 1),
                new Todo("B", "firstLater", ScheduledFor.now, 2),
                new Todo("C", "secondLater", ScheduledFor.later, 2));
    }

    @Test
    public void pull_whenThereAreAsManyNowTodosAsFocusSize_returnsNoLaterTodos() throws Exception {
        masterList.add("firstNow", ScheduledFor.now);
        masterList.add("secondNow", ScheduledFor.now);
        masterList.add("firstLater", ScheduledFor.later);
        masterList.add("secondLater", ScheduledFor.later);

        List<Todo> todos = masterList.pull();

        assertThat(todos).isEmpty();
        assertThat(masterList.getTodos()).containsExactly(
                new Todo("firstNow", ScheduledFor.now, 1),
                new Todo("secondNow", ScheduledFor.now, 2),
                new Todo("firstLater", ScheduledFor.later, 1),
                new Todo("secondLater", ScheduledFor.later, 2));
    }
}