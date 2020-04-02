package com.doerapispring.domain.events;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = true)
public class TodoDisplacedEvent extends TodoListEvent {
    private String todoId;
    private String task;

    public String getTodoId() {
        return todoId;
    }

    public String getTask() {
        return task;
    }

    public TodoDisplacedEvent(String userId,
                              String listId,
                              String todoId,
                              String task) {
        super(userId, listId);
        this.todoId = todoId;
        this.task = task;
    }

    public TodoDisplacedEvent() {
    }

    @Override
    public DomainEventType type() {
        return DomainEventType.TODO_DISPLACED;
    }
}
