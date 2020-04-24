package com.doerapispring.storage;

import com.doerapispring.domain.events.DomainEvent;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Entity
@Table(name = "list_events")
@AllArgsConstructor
@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
@EntityListeners(AuditingEntityListener.class)
public class TodoListEventStoreEntity {
    @EmbeddedId
    public TodoListEventStoreEntityKey key;

    @Column(name = "event_class")
    public Class<? extends DomainEvent> eventClass;

    @Column(name = "data")
    public String data;

    @Column(name = "created_at", updatable = false)
    @CreatedDate
    public Date createdAt;
}
