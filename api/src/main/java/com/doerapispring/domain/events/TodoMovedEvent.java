package com.doerapispring.domain.events;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = true)
public class TodoMovedEvent extends TodoListEvent {
    private String todoId;
    private String targetTodoId;

    public TodoMovedEvent(String userId,
                          String listId,
                          String todoId,
                          String targetTodoId) {
        super(userId, listId);
        this.todoId = todoId;
        this.targetTodoId = targetTodoId;
    }

    @SuppressWarnings("unused") // needed for deserializing using jackson
    public TodoMovedEvent() {
    }

    public String getTodoId() {
        return todoId;
    }

    public String getTargetTodoId() {
        return targetTodoId;
    }

    @Override
    public DomainEventType type() {
        return DomainEventType.TODO_MOVED;
    }
}
