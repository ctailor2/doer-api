package com.doerapispring.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.ResourceSupport;

class TodoListResponse extends ResourceSupport {
    @JsonProperty("list")
    private final TodoListDTO todoListDTO;

    TodoListResponse(TodoListDTO todoListDTO) {
        this.todoListDTO = todoListDTO;
    }

    TodoListDTO getTodoListDTO() {
        return todoListDTO;
    }
}
