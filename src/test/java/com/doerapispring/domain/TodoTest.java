package com.doerapispring.domain;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class TodoTest {
    @Test
    public void getLocalIdentifier_forTodoScheduledForNow() throws Exception {
        Todo todo = new Todo("someTask", ScheduledFor.now, 0);
        assertThat(todo.getLocalIdentifier()).isEqualTo("0i");
    }

    @Test
    public void getLocalIdentifier_forTodoScheduledForLater() throws Exception {
        Todo todo = new Todo("someTask", ScheduledFor.later, 0);
        assertThat(todo.getLocalIdentifier()).isEqualTo("0");
    }

    @Test
    public void getLocalIdentifier_forTodoScheduledForAnytime() throws Exception {
        Todo todo = new Todo("someTask", ScheduledFor.anytime, 0);
        assertThat(todo.getLocalIdentifier()).isEqualTo("0");
    }
}