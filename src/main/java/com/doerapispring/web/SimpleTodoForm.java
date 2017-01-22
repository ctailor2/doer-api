package com.doerapispring.web;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SimpleTodoForm {
    private final String task;

    public SimpleTodoForm(@JsonProperty("task") String task) {
        this.task = task;
    }

    public String getTask() {
        return task;
    }
}
