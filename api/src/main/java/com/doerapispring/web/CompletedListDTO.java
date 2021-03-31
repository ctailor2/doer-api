package com.doerapispring.web;

import org.springframework.hateoas.RepresentationModel;

import java.util.List;

public class CompletedListDTO extends RepresentationModel<CompletedListDTO> {
    private final List<CompletedTodoDTO> completedTodoDTOs;

    CompletedListDTO(List<CompletedTodoDTO> completedTodoDTOs) {
        this.completedTodoDTOs = completedTodoDTOs;
    }

    public List<CompletedTodoDTO> getTodos() {
        return completedTodoDTOs;
    }
}
