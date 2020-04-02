package com.doerapispring.domain.events;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = true)
public class TodoUpdatedEvent extends TodoListEvent {
    private String todoId;
    private String task;

    public TodoUpdatedEvent(String userId, String listId, String todoId, String task) {
        super(userId, listId);
        this.todoId = todoId;
        this.task = task;
    }

    @SuppressWarnings("unused") // needed for deserializing using jackson
    public TodoUpdatedEvent() {
    }

    public String getTodoId() {
        return todoId;
    }

    public String getTask() {
        return task;
    }

    @Override
    public DomainEventType type() {
        return DomainEventType.TODO_UPDATED;
    }
}
