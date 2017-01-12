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
}