package com.doerapispring.domain;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;


public class MasterListTest {
    private MasterList masterList;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Before
    public void setUp() throws Exception {
        masterList = new MasterList(new UniqueIdentifier("something"), 2);
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
}