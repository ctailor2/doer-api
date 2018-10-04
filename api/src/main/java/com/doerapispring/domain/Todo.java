package com.doerapispring.domain;

public class Todo {
    private TodoId todoId;
    private String task;

    public Todo(TodoId todoId, String task) {
        this.todoId = todoId;
        this.task = task;
    }

    public String getTask() {
        return task;
    }

    public TodoId getTodoId() {
        return todoId;
    }

    public void setTask(String task) {
        this.task = task;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Todo todo = (Todo) o;

        if (todoId != null ? !todoId.equals(todo.todoId) : todo.todoId != null) return false;
        return task != null ? task.equals(todo.task) : todo.task == null;
    }

    @Override
    public int hashCode() {
        int result = todoId != null ? todoId.hashCode() : 0;
        result = 31 * result + (task != null ? task.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Todo{" +
            "todoId=" + todoId +
            ", task='" + task + '\'' +
            '}';
    }
}
