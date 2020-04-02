package com.doerapispring.domain;

import com.doerapispring.domain.events.DomainEvent;

import java.util.List;

public interface DomainModel {
    List<? extends DomainEvent> getDomainEvents();

    void clearDomainEvents();
}
