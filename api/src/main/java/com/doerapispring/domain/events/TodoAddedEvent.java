package com.doerapispring.domain.events;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import static com.doerapispring.domain.events.DomainEventType.TODO_ADDED;

@ToString
@EqualsAndHashCode(callSuper = true)
public class TodoAddedEvent extends TodoListEvent {
    private String todoId;
    private String task;

    public TodoAddedEvent(String userId,
                          String listId,
                          String todoId,
                          String task) {
        super(userId, listId);
        this.todoId = todoId;
        this.task = task;
    }

    @SuppressWarnings("unused") // needed for deserializing using jackson
    public TodoAddedEvent() {
    }

    @Override
    public DomainEventType type() {
        return TODO_ADDED;
    }

    public String getTask() {
        return task;
    }

    public String getTodoId() {
        return todoId;
    }
}
