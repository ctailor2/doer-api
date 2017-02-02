package com.doerapispring.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.ResourceSupport;

public class TodoDTO extends ResourceSupport {
    private final String localIdentifier;
    private final String task;
    private final String scheduling;

    public TodoDTO(String localIdentifier, String task, String scheduling) {
        this.localIdentifier = localIdentifier;
        this.task = task;
        this.scheduling = scheduling;
    }

    @JsonProperty("id")
    public String getLocalIdentifier() {
        return localIdentifier;
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

        if (localIdentifier != null ? !localIdentifier.equals(todoDTO.localIdentifier) : todoDTO.localIdentifier != null) return false;
        if (task != null ? !task.equals(todoDTO.task) : todoDTO.task != null) return false;
        return scheduling != null ? scheduling.equals(todoDTO.scheduling) : todoDTO.scheduling == null;

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
        return "TodoDTO{" +
                "localIdentifier='" + localIdentifier + '\'' +
                ", task='" + task + '\'' +
                ", scheduling='" + scheduling + '\'' +
                '}';
    }
}
