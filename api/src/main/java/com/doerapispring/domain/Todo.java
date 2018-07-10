package com.doerapispring.domain;

public class Todo {
    private final String localIdentifier;
    private String task;
    private Integer position;

    public Todo(String localIdentifier, String task, Integer position) {
        this.localIdentifier = localIdentifier;
        this.task = task;
        this.position = position;
    }

    public String getTask() {
        return task;
    }

    public String getLocalIdentifier() {
        return localIdentifier;
    }

    public Integer getPosition() {
        return position;
    }

    public void setTask(String task) {
        this.task = task;
    }

    void setPosition(Integer position) {
        this.position = position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Todo todo = (Todo) o;

        if (localIdentifier != null ? !localIdentifier.equals(todo.localIdentifier) : todo.localIdentifier != null)
            return false;
        if (task != null ? !task.equals(todo.task) : todo.task != null) return false;
        return position != null ? position.equals(todo.position) : todo.position == null;
    }

    @Override
    public int hashCode() {
        int result = localIdentifier != null ? localIdentifier.hashCode() : 0;
        result = 31 * result + (task != null ? task.hashCode() : 0);
        result = 31 * result + (position != null ? position.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Todo{" +
            "localIdentifier='" + localIdentifier + '\'' +
            ", task='" + task + '\'' +
            ", position=" + position +
            '}';
    }
}
