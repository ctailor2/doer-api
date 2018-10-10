package com.doerapispring.web;

import org.springframework.hateoas.ResourceSupport;

import java.util.List;

public class CompletedListDTO extends ResourceSupport {
    private final List<CompletedTodoDTO> completedTodoDTOs;

    CompletedListDTO(List<CompletedTodoDTO> completedTodoDTOs) {
        this.completedTodoDTOs = completedTodoDTOs;
    }

    public List<CompletedTodoDTO> getTodos() {
        return completedTodoDTOs;
    }
}
