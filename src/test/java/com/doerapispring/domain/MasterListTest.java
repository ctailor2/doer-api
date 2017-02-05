package com.doerapispring.domain;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import static org.assertj.core.api.Assertions.assertThat;


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

    @Test
    public void update_whenTodoWithIdentifierExists_updatesTask() throws Exception {
        Todo todo = masterList.add("someTask", ScheduledFor.now);

        Todo updatedTodo = masterList.update(todo.getLocalIdentifier(), "aNewTask");

        assertThat(updatedTodo.getTask()).isEqualTo("aNewTask");
        assertThat(masterList.getTodos()).contains(updatedTodo);
    }

    @Test
    public void update_whenTodoWithIdentifierDoesNotExist_throwsNotFoundException() throws Exception {
        exception.expect(TodoNotFoundException.class);
        masterList.update("someBogusIdentifier", "coolNewTask");
    }

    @Test
    public void push_immediateTodo_toEmptyList_pushesIntoList_returnsTodoWithIdentifier_matchingItsPosition() throws Exception {
        Todo todo = masterList.push("someTask", ScheduledFor.now);

        assertThat(masterList.getTodos()).containsExactly(todo);
        assertThat(todo.getLocalIdentifier()).isEqualTo("0");
    }

    @Test
    public void push_postponedTodo_toEmptyList_pushesIntoList_returnsTodoWithIdentifier_matchingItsPosition() throws Exception {
        Todo todo = masterList.push("someTask", ScheduledFor.later);

        assertThat(masterList.getTodos()).containsExactly(todo);
        assertThat(todo.getLocalIdentifier()).isEqualTo("0");
    }

    @Test
    public void push_immediateTodo_toListWithImmediateTodos_addsTodoBefore() throws Exception {
        Todo firstTodo = masterList.add("something", ScheduledFor.now);

        Todo secondTodo = masterList.push("someTask", ScheduledFor.now);

        assertThat(masterList.getTodos()).containsExactly(secondTodo, firstTodo);
        assertThat(secondTodo.getLocalIdentifier()).isEqualTo("0");
    }

    @Test
    public void push_postponedTodo_toListWithImmediateTodos_addsTodoAfter() throws Exception {
        Todo firstTodo = masterList.add("something", ScheduledFor.now);

        Todo secondTodo = masterList.push("someTask", ScheduledFor.later);

        assertThat(masterList.getTodos()).containsExactly(firstTodo, secondTodo);
        assertThat(secondTodo.getLocalIdentifier()).isEqualTo("1");
    }

    @Test
    public void push_postponedTodo_toListWithPostponedTodos_addsTodoBefore() throws Exception {
        Todo firstTodo = masterList.add("something", ScheduledFor.later);

        Todo secondTodo = masterList.push("someTask", ScheduledFor.later);

        assertThat(masterList.getTodos()).containsExactly(secondTodo, firstTodo);
        assertThat(secondTodo.getLocalIdentifier()).isEqualTo("0");
    }

    @Test
    public void push_postponedTodo_toListWithImmediateTodos_addsTodoBeforeFirstPostponedTodo() throws Exception {
        Todo firstTodo = masterList.add("something", ScheduledFor.now);
        Todo secondTodo = masterList.add("somethingElse", ScheduledFor.later);

        Todo thirdTodo = masterList.push("someTask", ScheduledFor.later);

        assertThat(masterList.getTodos()).containsExactly(firstTodo, thirdTodo, secondTodo);
        assertThat(secondTodo.getLocalIdentifier()).isEqualTo("1");
    }

    @Test
    public void push_whenListAlreadyContainsTodo_doesNotAdd_throwsDuplicateTodoException() throws Exception {
        masterList.add("something", ScheduledFor.now);

        exception.expect(DuplicateTodoException.class);
        masterList.push("something", ScheduledFor.now);
    }

    @Test
    public void push_whenListIsFull__doesNotAdd_throwsListSizeExceededException() throws Exception {
        masterList.add("something", ScheduledFor.now);
        masterList.add("somethingElse", ScheduledFor.now);

        exception.expect(ListSizeExceededException.class);
        masterList.push("stillSomethingElse", ScheduledFor.now);
    }

    //    @Test
//    public void displace_whenTodoWithIdentifierExists() throws Exception {
//        Todo todo = masterList.add("someTask", ScheduledFor.now);
//
//        List<Todo> resultingTodos = masterList.displace(todo.getLocalIdentifier(), "someOtherTask");
//
//        assertThat(resultingTodos).isEqualTo(masterList.getTodos());
//        assertThat(masterList.getTodos())
//                .usingElementComparatorIgnoringFields("localIdentifier")
//                .contains(new Todo("ignoredId", "someTask", ScheduledFor.later));
//        assertThat(masterList.getTodos())
//                .contains(new Todo(todo.getLocalIdentifier(), "someOtherTask", ScheduledFor.now));
//    }
//
//    @Test
//    public void displace_whenTodoWithIdentifierExists_whenDisplacingTaskAlreadyExistsInImmediateList_throwsDuplicateTodoException() throws Exception {
//        Todo todo = masterList.add("someTask", ScheduledFor.now);
//        masterList.add("someOtherTask", ScheduledFor.now);
//
//        exception.expect(DuplicateTodoException.class);
//        masterList.displace(todo.getLocalIdentifier(), "someOtherTask");
//    }
//
//    @Test
//    public void displace_whenTodoWithIdentifierExists_whenDisplacedTaskAlreadyExistsInPostponedList_throwsDuplicateTodoException() throws Exception {
//        Todo todo = masterList.add("someTask", ScheduledFor.now);
//        masterList.add("someTask", ScheduledFor.later);
//
//        exception.expect(DuplicateTodoException.class);
//        masterList.displace(todo.getLocalIdentifier(), "someOtherTask");
//    }
//
//    @Test
//    public void displace_whenTodoWithIdentifierDoesNotExist_throwsNotFoundException() throws Exception {
//        exception.expect(TodoNotFoundException.class);
//        masterList.displace("someBogusIdentifier", "someTask");
//    }
}