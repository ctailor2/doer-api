package com.doerapispring.web;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.springframework.hateoas.ResourceSupport;

import java.util.List;

class CompletedTodosResponse extends ResourceSupport {
    @JsonProperty("todos")
    private final List<CompletedTodoDTO> completedTodoDTOs;

    CompletedTodosResponse(List<CompletedTodoDTO> completedTodoDTOs) {
        this.completedTodoDTOs = completedTodoDTOs;
    }

    List<CompletedTodoDTO> getCompletedTodoDTOs() {
        return completedTodoDTOs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CompletedTodosResponse that = (CompletedTodosResponse) o;

        return completedTodoDTOs != null ? completedTodoDTOs.equals(that.completedTodoDTOs) : that.completedTodoDTOs == null;

    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (completedTodoDTOs != null ? completedTodoDTOs.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TodosResponse{" +
                "completedTodoDTOs=" + completedTodoDTOs +
                '}';
    }
}
