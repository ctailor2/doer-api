package com.doerapispring.domain;

import com.doerapispring.domain.events.DomainEvent;

public interface DomainEventPublisher<Model, Event extends DomainEvent, OwnerId, Id> {
    Model publish(Model domainModel, Event domainEvent, OwnerId ownerId, Id id);
}
