package com.doerapispring.domain;

public class Todo {
    private final String localIdentifier;
    private String task;

    public Todo(String localIdentifier, String task) {
        this.localIdentifier = localIdentifier;
        this.task = task;
    }

    public String getTask() {
        return task;
    }

    public String getLocalIdentifier() {
        return localIdentifier;
    }

    public void setTask(String task) {
        this.task = task;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Todo todo = (Todo) o;

        if (localIdentifier != null ? !localIdentifier.equals(todo.localIdentifier) : todo.localIdentifier != null)
            return false;
        return task != null ? task.equals(todo.task) : todo.task == null;
    }

    @Override
    public int hashCode() {
        int result = localIdentifier != null ? localIdentifier.hashCode() : 0;
        result = 31 * result + (task != null ? task.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Todo{" +
            "localIdentifier='" + localIdentifier + '\'' +
            ", task='" + task + '\'' +
            '}';
    }
}
