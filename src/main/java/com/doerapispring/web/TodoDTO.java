package com.doerapispring.web;

public class TodoDTO {
    private final String id;
    private final String task;
    private final String scheduling;

    public TodoDTO(String id, String task, String scheduling) {
        this.id = id;
        this.task = task;
        this.scheduling = scheduling;
    }

    public String getId() {
        return id;
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

        if (id != null ? !id.equals(todoDTO.id) : todoDTO.id != null) return false;
        if (task != null ? !task.equals(todoDTO.task) : todoDTO.task != null) return false;
        return scheduling != null ? scheduling.equals(todoDTO.scheduling) : todoDTO.scheduling == null;

    }

    @Override
    public int hashCode() {
        int result = id != null ? id.hashCode() : 0;
        result = 31 * result + (task != null ? task.hashCode() : 0);
        result = 31 * result + (scheduling != null ? scheduling.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TodoDTO{" +
                "id='" + id + '\'' +
                ", task='" + task + '\'' +
                ", scheduling='" + scheduling + '\'' +
                '}';
    }
}
