package com.doerapispring.domain;

import java.util.Date;

public class CompletedTodo {
    private UserId userId;
    private CompletedTodoId completedTodoId;
    private String task;
    private Date completedAt;

    public CompletedTodo(UserId userId, CompletedTodoId completedTodoId, String task, Date completedAt) {
        this.userId = userId;
        this.completedTodoId = completedTodoId;
        this.task = task;
        this.completedAt = completedAt;
    }

    public CompletedTodoId getCompletedTodoId() {
        return completedTodoId;
    }

    public String getTask() {
        return task;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    public UserId getUserId() {
        return userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CompletedTodo that = (CompletedTodo) o;

        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
        if (completedTodoId != null ? !completedTodoId.equals(that.completedTodoId) : that.completedTodoId != null)
            return false;
        if (task != null ? !task.equals(that.task) : that.task != null) return false;
        return completedAt != null ? completedAt.equals(that.completedAt) : that.completedAt == null;
    }

    @Override
    public int hashCode() {
        int result = userId != null ? userId.hashCode() : 0;
        result = 31 * result + (completedTodoId != null ? completedTodoId.hashCode() : 0);
        result = 31 * result + (task != null ? task.hashCode() : 0);
        result = 31 * result + (completedAt != null ? completedAt.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CompletedTodo{" +
            "userId=" + userId +
            ", completedTodoId=" + completedTodoId +
            ", task='" + task + '\'' +
            ", completedAt=" + completedAt +
            '}';
    }
}
