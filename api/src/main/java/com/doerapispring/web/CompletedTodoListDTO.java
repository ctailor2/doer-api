package com.doerapispring.web;

import java.util.List;

public class CompletedTodoListDTO {
    private final List<CompletedTodoDTO> completedTodoDTOs;

    public CompletedTodoListDTO(List<CompletedTodoDTO> completedTodoDTOS) {
        this.completedTodoDTOs = completedTodoDTOS;
    }

    public List<CompletedTodoDTO> getCompletedTodoDTOs() {
        return completedTodoDTOs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompletedTodoListDTO that = (CompletedTodoListDTO) o;

        return completedTodoDTOs != null ? completedTodoDTOs.equals(that.completedTodoDTOs) : that.completedTodoDTOs == null;

    }

    @Override
    public int hashCode() {
        return completedTodoDTOs != null ? completedTodoDTOs.hashCode() : 0;
    }

    @Override
    public String toString() {
        return "CompletedTodoListDTO{" +
                "completedTodoDTOs=" + completedTodoDTOs +
                '}';
    }
}
