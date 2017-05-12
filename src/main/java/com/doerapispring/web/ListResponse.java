package com.doerapispring.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.ResourceSupport;

class ListResponse extends ResourceSupport {
    @JsonProperty("list")
    private final TodoListDTO todoListDTO;

    ListResponse(TodoListDTO todoListDTO) {
        this.todoListDTO = todoListDTO;
    }

    TodoListDTO getTodoListDTO() {
        return todoListDTO;
    }
}
