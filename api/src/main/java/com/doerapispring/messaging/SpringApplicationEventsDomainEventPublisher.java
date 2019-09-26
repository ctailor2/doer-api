package com.doerapispring.messaging;

import com.doerapispring.domain.DomainEvent;
import com.doerapispring.domain.DomainEventPublisher;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class SpringApplicationEventsDomainEventPublisher implements DomainEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringApplicationEventsDomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(List<DomainEvent> domainEvents) {
        domainEvents.forEach(domainEvent -> applicationEventPublisher.publishEvent(new PayloadApplicationEvent<>(domainEvent.getClass(), domainEvent)));
    }
}
