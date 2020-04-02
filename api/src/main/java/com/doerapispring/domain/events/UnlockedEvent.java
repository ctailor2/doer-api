package com.doerapispring.domain.events;

import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.util.Date;

@ToString
@EqualsAndHashCode(callSuper = true)
public class UnlockedEvent extends TodoListEvent {
    private Date unlockedAt;

    public UnlockedEvent(String userId,
                         String listId,
                         Date unlockedAt) {
        super(userId, listId);
        this.unlockedAt = unlockedAt;
    }

    @SuppressWarnings("unused") // needed for deserializing using jackson
    public UnlockedEvent() {
    }

    @Override
    public DomainEventType type() {
        return DomainEventType.UNLOCKED;
    }

    public Date getUnlockedAt() {
        return unlockedAt;
    }
}
