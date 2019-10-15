package com.doerapispring.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.ResourceSupport;

class TodoListReadModelResponse extends ResourceSupport {
    @JsonProperty("list")
    private final TodoListReadModelDTO todoListReadModelDTO;

    TodoListReadModelResponse(TodoListReadModelDTO todoListReadModelDTO) {
        this.todoListReadModelDTO = todoListReadModelDTO;
    }

    TodoListReadModelDTO getTodoListReadModelDTO() {
        return todoListReadModelDTO;
    }
}
