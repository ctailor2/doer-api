package com.doerapispring.domain;

import org.junit.Test;

import java.sql.Date;
import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;

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

        completedList.add("someTask");
        assertThat(completedList.getTodos()).hasSize(1);
        CompletedTodo completedTodo = completedList.getTodos().get(0);
        assertThat(completedTodo.getLocalIdentifier()).matches("\\w{8}-\\w{4}-\\w{4}-\\w{4}-\\w{12}");
        assertThat(completedTodo.getTask()).isEqualTo("someTask");
        assertThat(completedTodo.getCompletedAt()).isEqualTo(Date.from(now));
    }
}