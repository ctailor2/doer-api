package com.doerapispring.domain.events;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import static com.doerapispring.domain.events.DomainEventType.TODO_DELETED;

@ToString
@EqualsAndHashCode(callSuper = true)
public class TodoDeletedEvent extends TodoListEvent {
    private String todoId;

    public TodoDeletedEvent(String userId,
                            String listId,
                            String todoId) {
        super(userId, listId);
        this.todoId = todoId;
    }

    @SuppressWarnings("unused") // needed for deserializing using jackson
    public TodoDeletedEvent() {
    }

    @Override
    public DomainEventType type() {
        return TODO_DELETED;
    }

    public String getTodoId() {
        return todoId;
    }
}
