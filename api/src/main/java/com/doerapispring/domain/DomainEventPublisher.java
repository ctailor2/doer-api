package com.doerapispring.domain;

public interface DomainEventPublisher {
    void publish(DomainModel domainModel);
}
