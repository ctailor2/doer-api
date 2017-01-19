package com.doerapispring.domain;

import org.junit.Before;
import org.junit.Test;

import static org.fest.assertions.api.Assertions.assertThat;

public class MasterListTest {
    private MasterList masterList;

    @Before
    public void setUp() throws Exception {
        masterList = MasterList.newEmpty(new UniqueIdentifier("something"));
    }

    @Test
    public void add_immediateTodo_addsToMatchingList_returnsTodoWithIdentifier() throws Exception {
        Todo firstTodo = masterList.add("someTask", ScheduledFor.now);
        assertThat(masterList.getImmediateList().getTodos()).contains(firstTodo);
        assertThat(firstTodo.getLocalIdentifier()).isEqualTo("1i");
    }

    @Test
    public void add_postponedTodo_addsToMatchingList_returnsTodoWithIdentifier() throws Exception {
        Todo firstTodo = masterList.add("someTask", ScheduledFor.later);
        assertThat(masterList.getPostponedList().getTodos()).contains(firstTodo);
        assertThat(firstTodo.getLocalIdentifier()).isEqualTo("1");
    }

    @Test
    public void displace_movesMatchingImmediateTodoToPostponedList_replacesWithTask() throws Exception {
        Todo todo = masterList.add("someTask", ScheduledFor.now);
        masterList.displace(todo.getLocalIdentifier(), "aMoreImportantTask");
        assertThat(masterList.getImmediateList().getTodos().size()).isEqualTo(1);
        assertThat(masterList.getImmediateList().getTodos().get(0))
                .isEqualTo(new Todo("1i", "aMoreImportantTask", ScheduledFor.now));
        assertThat(masterList.getImmediateList().getTodos().size()).isEqualTo(1);
        assertThat(masterList.getImmediateList().getTodos().get(0))
                .isEqualTo(new Todo("1", "someTask", ScheduledFor.now));
    }
}