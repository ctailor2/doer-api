package com.doerapispring.domain;

import org.junit.Test;

import java.sql.Date;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class CompletedListTest {
    @Test
    public void add_addsTaskToList() {
        Clock clock = mock(Clock.class);
        Instant now = Instant.now();
        when(clock.instant()).thenReturn(now);
        CompletedList completedList = new CompletedList(clock, new UniqueIdentifier<>("someIdentifier"), new ArrayList<>());

        completedList.add(new CompletedTodoId("someId"), "someTask");

        List<CompletedTodo> completedListTodos = completedList.read().getTodos();
        assertThat(completedListTodos).containsExactly(
            new CompletedTodo(new CompletedTodoId("someId"), "someTask", Date.from(now)));
    }
}