package com.doerapispring.messaging;

import com.doerapispring.domain.DomainEventPublisher;
import com.doerapispring.domain.DomainModel;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.PayloadApplicationEvent;
import org.springframework.stereotype.Component;

@Component
public class SpringApplicationEventsDomainEventPublisher implements DomainEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    public SpringApplicationEventsDomainEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(DomainModel domainModel) {
        domainModel.getDomainEvents().forEach(domainEvent ->
            applicationEventPublisher.publishEvent(new PayloadApplicationEvent<>(domainEvent.getClass(), domainEvent)));
        domainModel.clearDomainEvents();
    }
}
