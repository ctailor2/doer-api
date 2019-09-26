package com.doerapispring.domain;

import java.util.List;

interface DomainModel {
    List<DomainEvent> getDomainEvents();

    void clearDomainEvents();
}
