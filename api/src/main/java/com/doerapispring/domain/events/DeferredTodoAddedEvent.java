package com.doerapispring.domain.events;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import static com.doerapispring.domain.events.DomainEventType.DEFERRED_TODO_ADDED;

@ToString
@EqualsAndHashCode(callSuper = true)
public class DeferredTodoAddedEvent extends TodoListEvent {
    private String todoId;
    private String task;

    public DeferredTodoAddedEvent(String userId,
                                  String listId,
                                  String todoId,
                                  String task) {
        super(userId, listId);
        this.todoId = todoId;
        this.task = task;
    }

    @SuppressWarnings("unused") // needed for deserializing using jackson
    public DeferredTodoAddedEvent() {
    }

    public String getTodoId() {
        return todoId;
    }

    public String getTask() {
        return task;
    }

    @Override
    public DomainEventType type() {
        return DEFERRED_TODO_ADDED;
    }
}
