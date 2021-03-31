package com.doerapispring.web;

import org.springframework.hateoas.RepresentationModel;

class TodoListDTO extends RepresentationModel<TodoListDTO> {
    private String name;

    TodoListDTO(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
