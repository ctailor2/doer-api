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
    public void add_whenListIsFull_doesNotAdd_throwsListSizeExceededException() throws Exception {
        masterList.add("someTask", ScheduledFor.now);
        masterList.add("someOtherTask", ScheduledFor.now);

        exception.expect(ListSizeExceededException.class);
        masterList.add("stillAnotherTask", ScheduledFor.now);
    }
}