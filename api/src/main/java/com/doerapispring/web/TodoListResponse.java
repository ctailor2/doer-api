package com.doerapispring.web;

import org.springframework.hateoas.ResourceSupport;

import java.util.List;

class TodoListResponse extends ResourceSupport {
    private List<TodoListDTO> lists;

    TodoListResponse(List<TodoListDTO> lists) {
        this.lists = lists;
    }

    public List<TodoListDTO> getLists() {
        return lists;
    }
}
