package com.doerapispring.web;

import org.springframework.hateoas.RepresentationModel;

import java.util.List;

class TodoListResponse extends RepresentationModel<TodoListResponse> {
    private List<TodoListDTO> lists;

    TodoListResponse(List<TodoListDTO> lists) {
        this.lists = lists;
    }

    public List<TodoListDTO> getLists() {
        return lists;
    }
}
