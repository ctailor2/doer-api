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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Todo todo = (Todo) o;

        if (task != null ? !task.equals(todo.task) : todo.task != null) return false;
        if (scheduling != todo.scheduling) return false;
        return userIdentifier != null ? userIdentifier.equals(todo.userIdentifier) : todo.userIdentifier == null;

    }

    @Override
    public int hashCode() {
        int result = task != null ? task.hashCode() : 0;
        result = 31 * result + (scheduling != null ? scheduling.hashCode() : 0);
        result = 31 * result + (userIdentifier != null ? userIdentifier.hashCode() : 0);
        return result;
    }
}
