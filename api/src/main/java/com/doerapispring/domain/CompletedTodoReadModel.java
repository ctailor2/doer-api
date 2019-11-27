package com.doerapispring.domain;

import java.util.Date;
import java.util.Objects;

public class CompletedTodoReadModel {
    private final CompletedTodoId completedTodoId;
    private final String task;
    private final Date completedAt;

    public CompletedTodoReadModel(CompletedTodoId completedTodoId,
                                  String task,
                                  Date completedAt) {
        this.completedTodoId = completedTodoId;
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
        CompletedTodoReadModel that = (CompletedTodoReadModel) o;
        return Objects.equals(completedTodoId, that.completedTodoId) &&
            Objects.equals(task, that.task) &&
            Objects.equals(completedAt, that.completedAt);
    }

    @Override
    public int hashCode() {
        return Objects.hash(completedTodoId, task, completedAt);
    }

    @Override
    public String toString() {
        return "CompletedTodoReadModel{" +
            "completedTodoId=" + completedTodoId +
            ", task='" + task + '\'' +
            ", completedAt=" + completedAt +
            '}';
    }
}
