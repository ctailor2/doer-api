package com.doerapispring.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.ResourceSupport;

public class TodoDTO extends ResourceSupport {
    private final String identifier;
    private final String task;

    public TodoDTO(String identifier, String task) {
        this.identifier = identifier;
        this.task = task;
    }

    @JsonProperty("id")
    public String getIdentifier() {
        return identifier;
    }

    public String getTask() {
        return task;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        TodoDTO todoDTO = (TodoDTO) o;

        if (identifier != null ? !identifier.equals(todoDTO.identifier) : todoDTO.identifier != null)
            return false;
        return task != null ? task.equals(todoDTO.task) : todoDTO.task == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (identifier != null ? identifier.hashCode() : 0);
        result = 31 * result + (task != null ? task.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TodoDTO{" +
            "identifier='" + identifier + '\'' +
            ", task='" + task + '\'' +
            '}';
    }
}
