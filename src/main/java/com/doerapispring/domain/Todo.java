package com.doerapispring.domain;

import com.fasterxml.jackson.annotation.JsonIgnore;

public class Todo {
    private final String task;
    private final ScheduledFor scheduling;

    @JsonIgnore
    private final UserIdentifier userIdentifier;

    public Todo(UserIdentifier userIdentifier,
                String task,
                ScheduledFor scheduling) {
        this.task = task;
        this.userIdentifier = userIdentifier;
        this.scheduling = scheduling;
    }

    public String getTask() {
        return task;
    }

    public UserIdentifier getUserIdentifier() {
        return userIdentifier;
    }

    public ScheduledFor getScheduling() {
        return scheduling;
    }
}
