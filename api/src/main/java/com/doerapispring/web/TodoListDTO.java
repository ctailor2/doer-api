package com.doerapispring.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.ResourceSupport;

import java.util.List;

public class TodoListDTO extends ResourceSupport {
    @JsonProperty("todos")
    private final List<TodoDTO> todoDTOs;

    public TodoListDTO(List<TodoDTO> todoDTOs) {
        this.todoDTOs = todoDTOs;
    }

    public List<TodoDTO> getTodoDTOs() {
        return todoDTOs;
    }
}
