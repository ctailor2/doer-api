package com.doerapispring.web;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TodoForm {
    private final String task;

    public TodoForm(@JsonProperty("task") String task) {
        this.task = task;
    }

    public String getTask() {
        return task;
    }
}
