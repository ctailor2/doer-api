package com.doerapispring.web;

import com.fasterxml.jackson.annotation.JsonFormat;
import org.springframework.hateoas.ResourceSupport;

import java.util.Date;

public class CompletedTodoDTO extends ResourceSupport {
    private final String task;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ssZ")
    private final Date completedAt;

    public CompletedTodoDTO(String task, Date completedAt) {
        this.task = task;
        this.completedAt = completedAt;
    }

    public String getTask() {
        return task;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        if (!super.equals(o)) return false;

        CompletedTodoDTO that = (CompletedTodoDTO) o;

        if (task != null ? !task.equals(that.task) : that.task != null) return false;
        return completedAt != null ? completedAt.equals(that.completedAt) : that.completedAt == null;
    }

    @Override
    public int hashCode() {
        int result = super.hashCode();
        result = 31 * result + (task != null ? task.hashCode() : 0);
        result = 31 * result + (completedAt != null ? completedAt.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CompletedTodoDTO{" +
            "task='" + task + '\'' +
            ", completedAt=" + completedAt +
            '}';
    }
}
