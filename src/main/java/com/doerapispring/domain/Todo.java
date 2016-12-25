package com.doerapispring.domain;

public class Todo {
    private final String task;
    private final ScheduledFor scheduling;

    public Todo(String task,
                ScheduledFor scheduling) {
        this.task = task;
        this.scheduling = scheduling;
    }

    public String getTask() {
        return task;
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
        return scheduling == todo.scheduling;

    }

    @Override
    public int hashCode() {
        int result = task != null ? task.hashCode() : 0;
        result = 31 * result + (scheduling != null ? scheduling.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Todo{" +
                "task='" + task + '\'' +
                ", scheduling=" + scheduling +
                '}';
    }
}
