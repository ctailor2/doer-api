package com.doerapispring.domain;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class TodoListTest {

    private ArrayList<Todo> todos;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        todos = new ArrayList<>();
    }

    @Test
    public void isFull_whenCountOfTodos_isGreaterThanOrEqualToMaxSize_returnsTrue() throws Exception {
        todos.add(new Todo("someTask", ScheduledFor.now, 1));
        TodoList todoList = new TodoList(ScheduledFor.now, todos, 0);

        assertThat(todoList.isFull()).isEqualTo(true);
    }

    @Test
    public void isFull_whenCountOfTodos_isLessThanMaxSize_returnsFalse() throws Exception {
        TodoList todoList = new TodoList(ScheduledFor.now, todos, 1);

        assertThat(todoList.isFull()).isEqualTo(false);
    }

    @Test
    public void isFull_whenMaxSizeIsNegative_alwaysReturnsFalse() throws Exception {
        TodoList todoList = new TodoList(ScheduledFor.now, todos, -2);

        assertThat(todoList.isFull()).isEqualTo(false);
    }

    @Test
    public void add_toEmptyList_addsToList_returnsTodoWithCorrectPosition() throws ListSizeExceededException {
        TodoList todoList = new TodoList(ScheduledFor.now, todos, 2);
        Todo firstTodo = todoList.add("someTask");
        assertThat(todoList.getTodos()).containsExactly(firstTodo);
        assertThat(firstTodo.getPosition()).isEqualTo(1);
    }

    @Test
    public void add_toListWithTodos_addsTodoAfterLast() throws Exception {
        TodoList todoList = new TodoList(ScheduledFor.now, todos, 2);
        Todo firstTodo = todoList.add("someTask");
        Todo secondTodo = todoList.add("someOtherTask");

        assertThat(todoList.getTodos()).containsExactly(firstTodo, secondTodo);
        assertThat(secondTodo.getPosition()).isEqualTo(2);
    }

    @Test
    public void add_whenListIsFull_doesNotAdd_throwsListSizeExceededException() throws Exception {
        TodoList todoList = new TodoList(ScheduledFor.now, todos, 2);
        todoList.add("someTask");
        todoList.add("someOtherTask");

        exception.expect(ListSizeExceededException.class);
        todoList.add("stillAnotherTask");
    }

    @Test
    public void addExisting_toEmptyList_addsToList_returnsTodoWithCorrectPosition() throws ListSizeExceededException {
        TodoList todoList = new TodoList(ScheduledFor.now, todos, 2);
        Todo firstTodo = todoList.addExisting("abc", "someTask");
        assertThat(todoList.getTodos()).containsExactly(firstTodo);
        assertThat(firstTodo.getPosition()).isEqualTo(1);
    }

    @Test
    public void addExisting_toListWithTodos_addsTodoAfterLast() throws Exception {
        TodoList todoList = new TodoList(ScheduledFor.now, todos, 2);
        Todo firstTodo = todoList.addExisting("abc", "someTask");
        Todo secondTodo = todoList.addExisting("def", "someOtherTask");

        assertThat(todoList.getTodos()).containsExactly(firstTodo, secondTodo);
        assertThat(secondTodo.getPosition()).isEqualTo(2);
    }

    @Test
    public void addExisting_whenListIsFull_doesNotAdd_throwsListSizeExceededException() throws Exception {
        TodoList todoList = new TodoList(ScheduledFor.now, todos, 2);
        todoList.addExisting("abc", "someTask");
        todoList.addExisting("def", "someOtherTask");

        exception.expect(ListSizeExceededException.class);
        todoList.addExisting("ghi", "stillAnotherTask");
    }

    @Test
    public void remove_removesTodoFromList() throws ListSizeExceededException {
        TodoList todoList = new TodoList(ScheduledFor.now, todos, 2);
        Todo todo = todoList.add("someTask");

        todoList.remove(todo);

        assertThat(todoList.getTodos()).doesNotContain(todo);
    }

    @Test
    public void move_whenTodoWithIdentifierExists_whenTargetExists_movesTodoDown() throws Exception {
        Todo fourthTodo = new Todo("D", "evenYetAnotherTask", ScheduledFor.later, 4);
        Todo firstTodo = new Todo("A", "someTask", ScheduledFor.later, 1);
        Todo thirdTodo = new Todo("C", "yetAnotherTask", ScheduledFor.later, 3);
        List<Todo> todos = asList(
                firstTodo,
                new Todo("B", "anotherTask", ScheduledFor.later, 2),
                thirdTodo,
                fourthTodo);

        TodoList laterList = new TodoList(ScheduledFor.later, todos, -1);

        List<Todo> effectedTodos = laterList.move(firstTodo, thirdTodo);

        Todo expectedFirstTodo = new Todo("B", "anotherTask", ScheduledFor.later, 1);
        Todo expectedSecondTodo = new Todo("C", "yetAnotherTask", ScheduledFor.later, 2);
        Todo expectedThirdTodo = new Todo("A", "someTask", ScheduledFor.later, 3);

        assertThat(effectedTodos).contains(expectedFirstTodo, expectedSecondTodo, expectedThirdTodo);
        assertThat(laterList.getTodos()).containsExactly(
                expectedFirstTodo,
                expectedSecondTodo,
                expectedThirdTodo,
                fourthTodo);
    }

    @Test
    public void move_whenTodoWithIdentifierExists_whenTargetExists_movesTodoUp() throws Exception {
        Todo firstTodo = new Todo("A", "someTask", ScheduledFor.later, 1);
        Todo fourthTodo = new Todo("D", "evenYetAnotherTask", ScheduledFor.later, 4);
        Todo secondTodo = new Todo("B", "anotherTask", ScheduledFor.later, 2);
        List<Todo> todos = asList(firstTodo,
                secondTodo,
                new Todo("C", "yetAnotherTask", ScheduledFor.later, 3),
                fourthTodo);

        TodoList laterList = new TodoList(ScheduledFor.later, todos, -1);

        List<Todo> effectedTodos = laterList.move(fourthTodo, secondTodo);

        Todo expectedSecondTodo = new Todo("D", "evenYetAnotherTask", ScheduledFor.later, 2);
        Todo expectedThirdTodo = new Todo("B", "anotherTask", ScheduledFor.later, 3);
        Todo expectedFourthTodo = new Todo("C", "yetAnotherTask", ScheduledFor.later, 4);

        assertThat(effectedTodos).contains(expectedSecondTodo, expectedThirdTodo, expectedFourthTodo);
        assertThat(laterList.getTodos()).containsExactly(
                firstTodo,
                expectedSecondTodo,
                expectedThirdTodo,
                expectedFourthTodo);
    }

    @Test
    public void move_beforeOrAfter_whenTodoWithIdentifierExists_whenTargetExists_whenOriginalAndTargetPositionsAreSame_doesNothing() throws Exception {
        Todo firstTodo = new Todo("A", "someTask", ScheduledFor.later, 1);
        List<Todo> todos = asList(
                firstTodo,
                new Todo("B", "anotherTask", ScheduledFor.later, 2),
                new Todo("C", "yetAnotherTask", ScheduledFor.later, 3),
                new Todo("D", "evenYetAnotherTask", ScheduledFor.later, 4));

        TodoList laterList = new TodoList(ScheduledFor.later, todos, -1);

        List<Todo> effectedTodos = laterList.move(firstTodo, firstTodo);

        assertThat(effectedTodos).isEmpty();
        assertThat(laterList.getTodos()).isEqualTo(todos);
    }

    @Test
    public void displace_whenThereIsASourceList_whenSourceListIsEmpty_replacesTodo_andPushesItIntoSourceListWithCorrectPositioning() throws Exception {
        Todo nowTodo = new Todo("someId", "someTask", ScheduledFor.now, 4);
        TodoList laterList = new TodoList(ScheduledFor.later, Collections.emptyList(), -1);
        TodoList nowList = new TodoList(ScheduledFor.now, Collections.singletonList(nowTodo), 3, laterList);

        List<Todo> todos = nowList.displace(nowTodo, "displace it");

        // TODO: The local identifier behavior here seems weird. Why should the new todo
        // get the id of the original todo that was displaced and that one get a newly assigned identifier?
        Todo displacedTodo = new Todo("0", "someTask", ScheduledFor.later, 1);
        Todo newTodo = new Todo("someId", "displace it", ScheduledFor.now, 4);
        assertThat(todos).contains(displacedTodo, newTodo);
        assertThat(nowList.getTodos()).containsOnly(newTodo);
    }

    @Test
    public void displace_whenThereIsASourceList_whenSourceListIsNotEmpty_replacesTodo_andPushesItIntoSourceListWithCorrectPositioning() throws Exception {
        Todo nowTodo = new Todo("someId", "someTask", ScheduledFor.now, 4);
        TodoList laterList = new TodoList(ScheduledFor.later, Collections.singletonList(new Todo("someOtherId", "someTask", ScheduledFor.later, 3)), -1);
        TodoList nowList = new TodoList(ScheduledFor.now, Collections.singletonList(nowTodo), 3, laterList);

        List<Todo> todos = nowList.displace(nowTodo, "displace it");

        Todo displacedTodo = new Todo("0", "someTask", ScheduledFor.later, 2);
        Todo newTodo = new Todo("someId", "displace it", ScheduledFor.now, 4);
        assertThat(todos).contains(displacedTodo, newTodo);
        assertThat(nowList.getTodos()).containsOnly(newTodo);
    }

    @Test
    public void displace_whenThereIsNoSourceList_throwsNoSourceListConfiguredException() throws NoSourceListConfiguredException {
        Todo nowTodo = new Todo("someId", "someTask", ScheduledFor.now, 4);
        TodoList nowList = new TodoList(ScheduledFor.now, Collections.singletonList(nowTodo), 3);

        exception.expect(NoSourceListConfiguredException.class);
        nowList.displace(nowTodo, "displace it");
    }

    @Test
    public void getByIdentifier_givenIdentifierForExistingTodo_returnsTodo() throws TodoNotFoundException {
        Todo nowTodo = new Todo("someId", "someTask", ScheduledFor.now, 4);
        TodoList nowList = new TodoList(ScheduledFor.now, Collections.singletonList(nowTodo), 2);

        Todo retrievedTodo = nowList.getByIdentifier("someId");

        assertThat(retrievedTodo).isEqualTo(nowTodo);
    }

    @Test
    public void getByIdentifier_givenIdentifierForNonExistentTodo_throwsTodoNotFoundException() throws TodoNotFoundException {
        TodoList nowList = new TodoList(ScheduledFor.now, Collections.emptyList(), 2);

        exception.expect(TodoNotFoundException.class);
        nowList.getByIdentifier("nonExistentId");
    }
}