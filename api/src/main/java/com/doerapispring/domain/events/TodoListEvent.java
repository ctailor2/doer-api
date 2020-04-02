package com.doerapispring.domain.events;

import lombok.EqualsAndHashCode;

@EqualsAndHashCode
public abstract class TodoListEvent implements DomainEvent {
    private String userId;
    private String listId;

    public TodoListEvent(String userId, String listId) {
        this.userId = userId;
        this.listId = listId;
    }

    public TodoListEvent() {
    }

    public String getUserId() {
        return userId;
    }

    public String getListId() {
        return listId;
    }
}
