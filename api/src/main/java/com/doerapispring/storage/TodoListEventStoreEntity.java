package com.doerapispring.storage;

import com.doerapispring.domain.events.DomainEvent;
import lombok.*;

import javax.persistence.Column;
import javax.persistence.EmbeddedId;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "list_events")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
public class TodoListEventStoreEntity {
    @EmbeddedId
    public TodoListEventStoreEntityKey key;

    @Column(name = "event_class")
    public Class<? extends DomainEvent> eventClass;

    @Column(name = "data")
    public String data;
}
