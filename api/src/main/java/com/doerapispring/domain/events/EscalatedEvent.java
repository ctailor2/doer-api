package com.doerapispring.domain.events;

import lombok.EqualsAndHashCode;
import lombok.ToString;

@ToString
@EqualsAndHashCode(callSuper = true)
public class EscalatedEvent extends TodoListEvent {
    public EscalatedEvent(String userId, String listId) {
        super(userId, listId);
    }

    @SuppressWarnings("unused") // needed for deserializing using jackson
    public EscalatedEvent() {
    }

    @Override
    public DomainEventType type() {
        return DomainEventType.ESCALATED;
    }
}
