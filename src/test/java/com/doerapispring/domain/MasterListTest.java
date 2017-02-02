package com.doerapispring.domain;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.fest.assertions.api.Assertions.assertThat;

public class MasterListTest {
    private MasterList masterList;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        masterList = MasterList.newEmpty(new UniqueIdentifier("something"));
    }

    @Test
    public void add_immediateTodo_toEmptyList_addsToList_returnsTodoWithIdentifier_matchingItsPosition() throws Exception {
        Todo firstTodo = masterList.add("someTask", ScheduledFor.now);
        assertThat(masterList.getTodos()).containsExactly(firstTodo);
        assertThat(firstTodo.getLocalIdentifier()).isEqualTo("0");
    }

    @Test
    public void add_immediateTodo_toListWithImmediateTodos_addsTodoAfterLast() throws Exception {
        Todo firstTodo = masterList.add("someTask", ScheduledFor.now);
        Todo secondTodo = masterList.add("someOtherTask", ScheduledFor.now);

        assertThat(masterList.getTodos()).containsExactly(firstTodo, secondTodo);
        assertThat(secondTodo.getLocalIdentifier()).isEqualTo("1");
    }

    @Test
    public void add_immediateTodo_toListWithPostponedTodos_addsTodoBefore() throws Exception {
        Todo firstTodo = masterList.add("someOtherTask", ScheduledFor.later);
        Todo secondTodo = masterList.add("stillSomeOtherTask", ScheduledFor.now);

        assertThat(masterList.getTodos()).containsExactly(secondTodo, firstTodo);
        assertThat(secondTodo.getLocalIdentifier()).isEqualTo("0");
    }

    @Test
    public void add_immediateTodo_toListWithPostponedTodos_addsTodoAfterLastImmediate_beforeFirstPostponed() throws Exception {
        Todo firstTodo = masterList.add("someTask", ScheduledFor.now);
        Todo secondTodo = masterList.add("someOtherTask", ScheduledFor.later);
        Todo thirdTodo = masterList.add("stillSomeOtherTask", ScheduledFor.now);

        assertThat(masterList.getTodos()).containsExactly(firstTodo, thirdTodo, secondTodo);
        assertThat(thirdTodo.getLocalIdentifier()).isEqualTo("1");
    }

    @Test
    public void add_postponedTodo_toEmptyList_addsToList_returnsTodoWithIdentifier_matchingItsPosition() throws Exception {
        Todo firstTodo = masterList.add("someTask", ScheduledFor.later);
        assertThat(masterList.getTodos()).containsExactly(firstTodo);
        assertThat(firstTodo.getLocalIdentifier()).isEqualTo("0");
    }

    @Test
    public void add_postponedTodo_toListWithPostponedTodos_addsTodoAfterLast() throws Exception {
        Todo firstTodo = masterList.add("someTask", ScheduledFor.later);
        Todo secondTodo = masterList.add("someOtherTask", ScheduledFor.later);

        assertThat(masterList.getTodos()).containsExactly(firstTodo, secondTodo);
        assertThat(secondTodo.getLocalIdentifier()).isEqualTo("1");
    }

    @Test
    public void add_postponedTodo_toListWithImmediateTodos_addsTodoAfter() throws Exception {
        Todo firstTodo = masterList.add("someTask", ScheduledFor.later);
        Todo secondTodo = masterList.add("someOtherTask", ScheduledFor.later);

        assertThat(masterList.getTodos()).containsExactly(firstTodo, secondTodo);
        assertThat(secondTodo.getLocalIdentifier()).isEqualTo("1");
    }

    @Test
    public void add_whenListAlreadyContainsTodo_doesNotAdd_throwsDuplicateTodoException() throws Exception {
        masterList.add("someTask", ScheduledFor.now);

        exception.expect(DuplicateTodoException.class);
        masterList.add("someTask", ScheduledFor.now);

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
}