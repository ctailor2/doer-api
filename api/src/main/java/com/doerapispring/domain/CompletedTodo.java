package com.doerapispring.domain;

import java.util.Date;

public class CompletedTodo {
    private final String localIdentifier;
    private final String task;
    private final Date completedAt;

    public CompletedTodo(String localIdentifier, String task, Date completedAt) {
        this.localIdentifier = localIdentifier;
        this.task = task;
        this.completedAt = completedAt;
    }

    public String getLocalIdentifier() {
        return localIdentifier;
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

        CompletedTodo that = (CompletedTodo) o;

        if (localIdentifier != null ? !localIdentifier.equals(that.localIdentifier) : that.localIdentifier != null)
            return false;
        if (task != null ? !task.equals(that.task) : that.task != null) return false;
        return completedAt != null ? completedAt.equals(that.completedAt) : that.completedAt == null;
    }

    @Override
    public int hashCode() {
        int result = localIdentifier != null ? localIdentifier.hashCode() : 0;
        result = 31 * result + (task != null ? task.hashCode() : 0);
        result = 31 * result + (completedAt != null ? completedAt.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CompletedTodo{" +
            "localIdentifier='" + localIdentifier + '\'' +
            ", task='" + task + '\'' +
            ", completedAt=" + completedAt +
            '}';
    }
}
