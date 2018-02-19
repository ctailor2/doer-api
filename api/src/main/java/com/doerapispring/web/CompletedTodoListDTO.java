package com.doerapispring.web;

import java.util.List;

public class CompletedTodoListDTO {
    private final List<TodoDTO> todoDTOs;

    public CompletedTodoListDTO(List<TodoDTO> todoDTOs) {
        this.todoDTOs = todoDTOs;
    }

    public List<TodoDTO> getTodoDTOs() {
        return todoDTOs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompletedTodoListDTO that = (CompletedTodoListDTO) o;

        return todoDTOs != null ? todoDTOs.equals(that.todoDTOs) : that.todoDTOs == null;

    }

    @Override
    public int hashCode() {
        return todoDTOs != null ? todoDTOs.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "CompletedTodoListDTO{" +
                "todoDTOs=" + todoDTOs +
                '}';
    }
}
