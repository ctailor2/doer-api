package com.doerapispring.domain;

import java.util.List;

public interface DomainEventPublisher {
    void publish(List<DomainEvent> domainEvents);
}
