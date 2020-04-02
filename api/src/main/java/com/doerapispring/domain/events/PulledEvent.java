package com.doerapispring.domain.events;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import static com.doerapispring.domain.events.DomainEventType.PULLED;

@ToString
@EqualsAndHashCode(callSuper = true)
public class PulledEvent extends TodoListEvent {
    public PulledEvent(String userId,
                       String listId) {
        super(userId, listId);
    }

    @SuppressWarnings("unused") // needed for deserializing using jackson
    public PulledEvent() {
    }

    @Override
    public DomainEventType type() {
        return PULLED;
    }
}
