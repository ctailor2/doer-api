package com.doerapispring.domain;

import java.util.Date;

public class TodoList {
    private final UserId userId;
    private final ListId listId;
    private final String name;
    private final Integer demarcationIndex;
    private final Date lastUnlockedAt;

    public TodoList(UserId userId, ListId listId, String name, Integer demarcationIndex, Date lastUnlockedAt) {
        this.userId = userId;
        this.listId = listId;
        this.name = name;
        this.demarcationIndex = demarcationIndex;
        this.lastUnlockedAt = lastUnlockedAt;
    }

    public UserId getUserId() {
        return userId;
    }

    public ListId getListId() {
        return listId;
    }

    public String getName() {
        return name;
    }

    public Integer getDemarcationIndex() {
        return demarcationIndex;
    }

    public Date getLastUnlockedAt() {
        return lastUnlockedAt;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TodoList that = (TodoList) o;

        if (userId != null ? !userId.equals(that.userId) : that.userId != null) return false;
        if (listId != null ? !listId.equals(that.listId) : that.listId != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (demarcationIndex != null ? !demarcationIndex.equals(that.demarcationIndex) : that.demarcationIndex != null)
            return false;
        return lastUnlockedAt != null ? lastUnlockedAt.equals(that.lastUnlockedAt) : that.lastUnlockedAt == null;
    }

    @Override
    public int hashCode() {
        int result = userId != null ? userId.hashCode() : 0;
        result = 31 * result + (listId != null ? listId.hashCode() : 0);
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (demarcationIndex != null ? demarcationIndex.hashCode() : 0);
        result = 31 * result + (lastUnlockedAt != null ? lastUnlockedAt.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "TodoList{" +
            "userId=" + userId +
            ", listId=" + listId +
            ", name='" + name + '\'' +
            ", demarcationIndex=" + demarcationIndex +
            ", lastUnlockedAt=" + lastUnlockedAt +
            '}';
    }
}
