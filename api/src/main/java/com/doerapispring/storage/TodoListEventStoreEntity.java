package com.doerapispring.storage;

import com.doerapispring.domain.events.TodoListEvent;
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

    public TodoListEventStoreEntity(TodoListEventStoreEntityKey key, Class<? extends TodoListEvent> eventClass, String data) {
        this.key = key;
        this.eventClass = eventClass;
        this.data = data;
    }

    @EmbeddedId
    public TodoListEventStoreEntityKey key;

    @Column(name = "event_class")
    public Class<? extends TodoListEvent> eventClass;

    @Column(name = "data")
    public String data;

    @Column(name = "created_at", updatable = false)
    @CreatedDate
    public Date createdAt;
}
