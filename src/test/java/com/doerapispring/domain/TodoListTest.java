package com.doerapispring.domain;

import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

public class TodoListTest {
    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void isFull_whenCountOfTodos_isGreaterThanOrEqualToMaxSize_returnsTrue() throws Exception {
        TodoList todoList = new TodoList(MasterList.NAME, 0);

        assertThat(todoList.isFull()).isEqualTo(true);
    }

    @Test
    public void isFull_whenCountOfTodos_isLessThanMaxSize_returnsFalse() throws Exception {
        TodoList todoList = new TodoList(MasterList.NAME, 1);

        assertThat(todoList.isFull()).isEqualTo(false);
    }

    @Test
    public void isFull_whenMaxSizeIsNegative_alwaysReturnsFalse() throws Exception {
        TodoList todoList = new TodoList(MasterList.NAME, -2);

        assertThat(todoList.isFull()).isEqualTo(false);
    }

    @Test
    public void add_toEmptyList_addsToList_returnsTodoWithUuidStyleIdentifier_andCorrectPosition() throws ListSizeExceededException {
        TodoList todoList = new TodoList(MasterList.NAME, 2);
        Todo firstTodo = todoList.add("someTask");
        assertThat(todoList.getTodos()).containsExactly(firstTodo);
        assertThat(firstTodo.getPosition()).isEqualTo(1);
        assertThat(firstTodo.getLocalIdentifier()).matches("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}");
    }

    @Test
    public void add_toListWithTodos_addsTodoAfterLast() throws Exception {
        TodoList todoList = new TodoList(MasterList.NAME, 2);
        Todo firstTodo = todoList.add("someTask");
        Todo secondTodo = todoList.add("someOtherTask");

        assertThat(todoList.getTodos()).containsExactly(firstTodo, secondTodo);
        assertThat(secondTodo.getPosition()).isEqualTo(2);
    }

    @Test
    public void add_whenListIsFull_doesNotAdd_throwsListSizeExceededException() throws Exception {
        TodoList todoList = new TodoList(MasterList.NAME, 2);
        todoList.add("someTask");
        todoList.add("someOtherTask");

        exception.expect(ListSizeExceededException.class);
        todoList.add("stillAnotherTask");
    }

    @Test
    public void addExisting_toEmptyList_addsToList_returnsTodoWithCorrectPosition() throws ListSizeExceededException {
        TodoList todoList = new TodoList(MasterList.NAME, 2);
        Todo firstTodo = todoList.addExisting("abc", "someTask");
        assertThat(todoList.getTodos()).containsExactly(firstTodo);
        assertThat(firstTodo.getPosition()).isEqualTo(1);
    }

    @Test
    public void addExisting_toListWithTodos_addsTodoAfterLast() throws Exception {
        TodoList todoList = new TodoList(MasterList.NAME, 2);
        Todo firstTodo = todoList.addExisting("abc", "someTask");
        Todo secondTodo = todoList.addExisting("def", "someOtherTask");

        assertThat(todoList.getTodos()).containsExactly(firstTodo, secondTodo);
        assertThat(secondTodo.getPosition()).isEqualTo(2);
    }

    @Test
    public void addExisting_whenListIsFull_doesNotAdd_throwsListSizeExceededException() throws Exception {
        TodoList todoList = new TodoList(MasterList.NAME, 2);
        todoList.addExisting("abc", "someTask");
        todoList.addExisting("def", "someOtherTask");

        exception.expect(ListSizeExceededException.class);
        todoList.addExisting("ghi", "stillAnotherTask");
    }

    @Test
    public void remove_removesTodoFromList() throws ListSizeExceededException {
        TodoList todoList = new TodoList(MasterList.NAME, 2);
        Todo todo = todoList.add("someTask");

        todoList.remove(todo);

        assertThat(todoList.getTodos()).doesNotContain(todo);
    }

    @Test
    public void move_whenTodoWithIdentifierExists_whenTargetExists_movesTodoDown() throws Exception {
        List<String> tasks = Arrays.asList(
            "someTask",
            "anotherTask",
            "yetAnotherTask",
            "evenYetAnotherTask");

        TodoList laterList = new TodoList(MasterList.DEFERRED_NAME, -1);

        List<Todo> todos = new ArrayList<>();
        for (String task : tasks) {
            todos.add(laterList.add(task));
        }
        Todo firstTodo = todos.get(0);
        Todo secondTodo = todos.get(1);
        Todo thirdTodo = todos.get(2);

        List<Todo> effectedTodos = laterList.move(firstTodo, thirdTodo);

        assertThat(effectedTodos).containsExactly(secondTodo, thirdTodo, firstTodo);
        assertThat(laterList.getTodos()).containsExactly(
            secondTodo,
            thirdTodo,
            firstTodo,
            todos.get(3));
    }

    @Test
    public void move_whenTodoWithIdentifierExists_whenTargetExists_movesTodoUp() throws Exception {
        List<String> tasks = asList(
            "someTask",
            "anotherTask",
            "yetAnotherTask",
            "evenYetAnotherTask");

        TodoList laterList = new TodoList(MasterList.DEFERRED_NAME, -1);

        List<Todo> todos = new ArrayList<>();
        for (String task : tasks) {
            todos.add(laterList.add(task));
        }

        Todo secondTodo = todos.get(1);
        Todo thirdTodo = todos.get(2);
        Todo fourthTodo = todos.get(3);

        List<Todo> effectedTodos = laterList.move(fourthTodo, secondTodo);

        assertThat(effectedTodos).containsExactly(fourthTodo, secondTodo, thirdTodo);
        assertThat(laterList.getTodos()).containsExactly(
            todos.get(0),
            fourthTodo,
            secondTodo,
            thirdTodo);
    }

    @Test
    public void move_beforeOrAfter_whenTodoWithIdentifierExists_whenTargetExists_whenOriginalAndTargetPositionsAreSame_doesNothing() throws Exception {
        List<String> tasks = asList(
            "someTask",
            "anotherTask",
            "yetAnotherTask",
            "evenYetAnotherTask"
        );

        TodoList laterList = new TodoList(MasterList.DEFERRED_NAME, -1);

        List<Todo> todos = new ArrayList<>();
        for (String task : tasks) {
            todos.add(laterList.add(task));
        }

        Todo firstTodo = todos.get(0);

        List<Todo> effectedTodos = laterList.move(firstTodo, firstTodo);

        assertThat(effectedTodos).isEmpty();
        assertThat(laterList.getTodos()).isEqualTo(todos);
    }

    @Test
    public void getByIdentifier_givenIdentifierForExistingTodo_returnsTodo() throws Exception {
        TodoList nowList = new TodoList(MasterList.NAME, 2);
        Todo todo = nowList.add("someTask");

        Todo retrievedTodo = nowList.getByIdentifier(todo.getLocalIdentifier());

        assertThat(retrievedTodo).isEqualTo(todo);
    }

    @Test
    public void getByIdentifier_givenIdentifierForNonExistentTodo_throwsTodoNotFoundException() throws TodoNotFoundException {
        TodoList nowList = new TodoList(MasterList.NAME, 2);

        exception.expect(TodoNotFoundException.class);
        nowList.getByIdentifier("nonExistentId");
    }
}