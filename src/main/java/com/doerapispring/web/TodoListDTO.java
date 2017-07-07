package com.doerapispring.web;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.ResourceSupport;

import java.util.List;

public class TodoListDTO extends ResourceSupport {
    @JsonProperty("todos")
    private final List<TodoDTO> todoDTOs;

    @JsonIgnore
    private final boolean full;

    public TodoListDTO(List<TodoDTO> todoDTOs, boolean full) {
        this.todoDTOs = todoDTOs;
        this.full = full;
    }

    public boolean isFull() {
        return full;
    }

    List<TodoDTO> getTodoDTOs() {
        return todoDTOs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        TodoListDTO that = (TodoListDTO) o;

        if (full != that.full) return false;
        return todoDTOs != null ? todoDTOs.equals(that.todoDTOs) : that.todoDTOs == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (todoDTOs != null ? todoDTOs.hashCode() : 0);
        result = 31 * result + (full ? 1 : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TodoListDTO{" +
            "todoDTOs=" + todoDTOs +
            ", full=" + full +
            '}';
    }
}
