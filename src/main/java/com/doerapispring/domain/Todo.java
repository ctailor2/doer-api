package com.doerapispring.domain;

public class Todo {
    private final String localIdentifier;
    private String task;
    private final ScheduledFor scheduling;

    public Todo(String localIdentifier, String task, ScheduledFor scheduling) {
        this.localIdentifier = localIdentifier;
        this.task = task;
        this.scheduling = scheduling;
    }

    public String getTask() {
        return task;
    }

    public ScheduledFor getScheduling() {
        return scheduling;
    }

    public String getLocalIdentifier() {
        return localIdentifier;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Todo todo = (Todo) o;

        if (localIdentifier != null ? !localIdentifier.equals(todo.localIdentifier) : todo.localIdentifier != null)
            return false;
        if (task != null ? !task.equals(todo.task) : todo.task != null) return false;
        return scheduling == todo.scheduling;

    }

    @Override
    public int hashCode() {
        int result = localIdentifier != null ? localIdentifier.hashCode() : 0;
        result = 31 * result + (task != null ? task.hashCode() : 0);
        result = 31 * result + (scheduling != null ? scheduling.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Todo{" +
                "localIdentifier='" + localIdentifier + '\'' +
                ", task='" + task + '\'' +
                ", scheduling=" + scheduling +
                '}';
    }

    public void setTask(String task) {
        this.task = task;
    }
}
