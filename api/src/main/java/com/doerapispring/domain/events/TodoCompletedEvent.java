package com.doerapispring.domain.events;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

import static com.doerapispring.domain.events.DomainEventType.TODO_COMPLETED;

@ToString
@EqualsAndHashCode(callSuper = true)
public class TodoCompletedEvent extends TodoListEvent {
    private String completedTodoId;
    private String task;
    private Date completedAt;

    public TodoCompletedEvent(String userId,
                              String listId,
                              String completedTodoId,
                              String task,
                              Date completedAt) {
        super(userId, listId);
        this.completedTodoId = completedTodoId;
        this.task = task;
        this.completedAt = completedAt;
    }

    @SuppressWarnings("unused") // needed for deserializing using jackson
    public TodoCompletedEvent() {
    }

    public String getCompletedTodoId() {
        return completedTodoId;
    }

    public String getTask() {
        return task;
    }

    public Date getCompletedAt() {
        return completedAt;
    }

    @Override
    public DomainEventType type() {
        return TODO_COMPLETED;
    }
}
