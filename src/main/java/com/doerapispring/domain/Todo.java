package com.doerapispring.domain;

public class Todo {
    private String task;
    private boolean complete = false;
    private Integer position;
    private final String localIdentifier;
    private String listName;

    // TODO: Remove this constructor
    public Todo(String task, String listName, Integer position) {
        this.localIdentifier = "0";
        this.task = task;
        this.listName = listName;
        this.position = position;
    }

    public Todo(String localIdentifier, String task, String listName, Integer position) {
        this.localIdentifier = localIdentifier;
        this.task = task;
        this.listName = listName;
        this.position = position;
    }

    public String getTask() {
        return task;
    }

    public String getListName() {
        return listName;
    }

    public String getLocalIdentifier() {
        return localIdentifier;
    }

    public Integer getPosition() {
        return position;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Todo todo = (Todo) o;

        if (complete != todo.complete) return false;
        if (task != null ? !task.equals(todo.task) : todo.task != null) return false;
        if (position != null ? !position.equals(todo.position) : todo.position != null) return false;
        if (localIdentifier != null ? !localIdentifier.equals(todo.localIdentifier) : todo.localIdentifier != null)
            return false;
        return listName != null ? listName.equals(todo.listName) : todo.listName == null;

    }

    @Override
    public int hashCode() {
        int result = task != null ? task.hashCode() : 0;
        result = 31 * result + (complete ? 1 : 0);
        result = 31 * result + (position != null ? position.hashCode() : 0);
        result = 31 * result + (localIdentifier != null ? localIdentifier.hashCode() : 0);
        result = 31 * result + (listName != null ? listName.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Todo{" +
            "task='" + task + '\'' +
            ", complete=" + complete +
            ", position=" + position +
            ", localIdentifier='" + localIdentifier + '\'' +
            ", listName='" + listName + '\'' +
            '}';
    }

    public void setTask(String task) {
        this.task = task;
    }

    public void complete() {
        this.complete = true;
    }

    public boolean isComplete() {
        return complete;
    }

    void setPosition(Integer position) {
        this.position = position;
    }
}
