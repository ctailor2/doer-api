package com.doerapispring.domain;

import org.junit.Before;
import org.junit.Test;

import java.util.ArrayList;

import static org.assertj.core.api.Java6Assertions.assertThat;

public class TodoListTest {

    private ArrayList<Todo> todos;

    @Before
    public void setUp() throws Exception {
        todos = new ArrayList<>();
    }

    @Test
    public void isFull_whenCountOfTodos_isGreaterThanOrEqualToMaxSize_returnsTrue() throws Exception {
        todos.add(new Todo("someTask", ScheduledFor.now, 1));
        TodoList todoList = new TodoList(ScheduledFor.now, todos, 0);

        assertThat(todoList.isFull()).isEqualTo(true);
    }

    @Test
    public void isFull_whenCountOfTodos_isLessThanMaxSize_returnsFalse() throws Exception {
        TodoList todoList = new TodoList(ScheduledFor.now, todos, 1);

        assertThat(todoList.isFull()).isEqualTo(false);
    }

    @Test
    public void isFull_whenMaxSizeIsNegative_alwaysReturnsFalse() throws Exception {
        TodoList todoList = new TodoList(ScheduledFor.now, todos, -2);

        assertThat(todoList.isFull()).isEqualTo(false);
    }
}