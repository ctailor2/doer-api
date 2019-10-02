package com.doerapispring.domain;

import java.util.Date;

public class TodoCompletedEvent implements DomainEvent {
    private final UserId userId;
    private final ListId listId;
    private final CompletedTodoId completedTodoId;
    private final String task;
    private final Date completedAt;

    TodoCompletedEvent(UserId userId, ListId listId, CompletedTodoId completedTodoId, String task, Date completedAt) {
        this.userId = userId;
        this.listId = listId;
        this.completedTodoId = completedTodoId;
        this.task = task;
        this.completedAt = completedAt;
    }

    public UserId getUserId() {
        return userId;
    }

    public ListId getListId() {
        return listId;
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

    @Override
    public String toString() {
        return "TodoCompletedEvent{" +
            "userId=" + userId +
            ", listId=" + listId +
            ", completedTodoId=" + completedTodoId +
            ", task='" + task + '\'' +
            ", completedAt=" + completedAt +
            '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TodoCompletedEvent that = (TodoCompletedEvent) o;

        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
        if (listId != null ? !listId.equals(that.listId) : that.listId != null) return false;
        if (completedTodoId != null ? !completedTodoId.equals(that.completedTodoId) : that.completedTodoId != null)
            return false;
        if (task != null ? !task.equals(that.task) : that.task != null) return false;
        return completedAt != null ? completedAt.equals(that.completedAt) : that.completedAt == null;
    }

    @Override
    public int hashCode() {
        int result = userId != null ? userId.hashCode() : 0;
        result = 31 * result + (listId != null ? listId.hashCode() : 0);
        result = 31 * result + (completedTodoId != null ? completedTodoId.hashCode() : 0);
        result = 31 * result + (task != null ? task.hashCode() : 0);
        result = 31 * result + (completedAt != null ? completedAt.hashCode() : 0);
        return result;
    }
}
