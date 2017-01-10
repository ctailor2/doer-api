package com.doerapispring.web;

import com.fasterxml.jackson.annotation.JsonProperty;

public class TodoForm {
    private final String task;
    private final String scheduling;

    public TodoForm(@JsonProperty("task") String task,
                    @JsonProperty("scheduling") String scheduling) {
        this.task = task;
        this.scheduling = scheduling;
    }

    public String getTask() {
        return task;
    }

    public String getScheduling() {
        return scheduling;
    }
}
