package com.doerapispring.domain;

public class Todo {
    private String task;
    private final ScheduledFor scheduling;
    private final Integer position;

    public Todo(String task, ScheduledFor scheduling, Integer position) {
        this.task = task;
        this.scheduling = scheduling;
        this.position = position;
    }

    public String getTask() {
        return task;
    }

    public ScheduledFor getScheduling() {
        return scheduling;
    }

    public String getLocalIdentifier() {
        return String.format("%d%s", position, getTodoIdentifierSuffix());
    }

    public Integer getPosition() {
        return position;
    }

    private String getTodoIdentifierSuffix() {
        if (ScheduledFor.now.equals(scheduling)) {
            return "i";
        }
        return "";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Todo todo = (Todo) o;

        if (task != null ? !task.equals(todo.task) : todo.task != null) return false;
        if (scheduling != todo.scheduling) return false;
        return position != null ? position.equals(todo.position) : todo.position == null;

    }

    @Override
    public int hashCode() {
        int result = task != null ? task.hashCode() : 0;
        result = 31 * result + (scheduling != null ? scheduling.hashCode() : 0);
        result = 31 * result + (position != null ? position.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Todo{" +
                "task='" + task + '\'' +
                ", scheduling=" + scheduling +
                ", position=" + position +
                '}';
    }

    public void setTask(String task) {
        this.task = task;
    }
}
