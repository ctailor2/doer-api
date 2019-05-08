package com.doerapispring.storage;

import lombok.*;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.util.Date;

@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode(callSuper = true)
@ToString
@Table(value = "completed_todos")
class CompletedTodoEntity extends UuidIdentifiable {
    @Column(value = "user_id")
    public Long userId;

    @Column(value = "task")
    public String task;

    @Column(value = "completed_at")
    public Date completedAt;

    @Column(value = "list_id")
    public String listId;

    @Builder
    public CompletedTodoEntity(String id, Long userId, String task, Date completedAt, String listId) {
        this.id = id;
        this.userId = userId;
        this.task = task;
        this.completedAt = completedAt;
        this.listId = listId;
    }

    CompletedTodoEntity withUuid(String uuid) {
        this.uuid = uuid;
        return this;
    }
}
