package com.doerapispring.web;

import org.springframework.hateoas.ResourceSupport;

class TodoListDTO extends ResourceSupport {
    private String name;

    TodoListDTO(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
