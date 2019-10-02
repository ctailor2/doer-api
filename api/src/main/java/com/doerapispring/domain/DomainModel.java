package com.doerapispring.domain;

import java.util.List;

public interface DomainModel {
    List<DomainEvent> getDomainEvents();

    void clearDomainEvents();
}
