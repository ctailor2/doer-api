package com.doerapispring.storage;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Builder
@Table(value = "completed_todos")
class CompletedTodoEntity implements Persistable<String> {
    @Id
    @Column(value = "uuid")
    public String uuid;

    @Column(value = "user_identifier")
    public String userIdentifier;

    @Column(value = "task")
    public String task;

    @Column(value = "completed_at")
    public Date completedAt;

    @Column(value = "list_id")
    public String listId;

    @Override
    public String getId() {
        return uuid;
    }

    @Override
    public boolean isNew() {
        return true;
    }
}
