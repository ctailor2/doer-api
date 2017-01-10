package com.doerapispring.web;

public class TodoDTO {
    private final String task;
    private final String scheduling;

    public TodoDTO(String task, String scheduling) {
        this.task = task;
        this.scheduling = scheduling;
    }

    public String getTask() {
        return task;
    }

    public String getScheduling() {
        return scheduling;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TodoDTO todoDTO = (TodoDTO) o;

        if (task != null ? !task.equals(todoDTO.task) : todoDTO.task != null) return false;
        return scheduling != null ? scheduling.equals(todoDTO.scheduling) : todoDTO.scheduling == null;

    }

    @Override
    public int hashCode() {
        int result = task != null ? task.hashCode() : 0;
        result = 31 * result + (scheduling != null ? scheduling.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TodoDTO{" +
                "task='" + task + '\'' +
                ", scheduling='" + scheduling + '\'' +
                '}';
    }
}
