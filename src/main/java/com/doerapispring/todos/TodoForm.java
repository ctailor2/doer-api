package com.doerapispring.todos;

import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * Created by chiragtailor on 12/4/16.
 */
public class TodoForm {
    private final String task;
    private final ScheduledFor scheduling;

    public TodoForm(@JsonProperty("task") String task,
                    @JsonProperty("scheduling") ScheduledFor scheduling) {
        this.task = task;
        this.scheduling = scheduling;
    }

    public String getTask() {
        return task;
    }

    public ScheduledFor getScheduling() {
        return scheduling;
    }
}
