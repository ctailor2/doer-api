package com.doerapispring.storage;

import lombok.*;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Entity
@Table(name = "completed_todos")
class CompletedTodoEntity {
    @Id
    @Column(name = "uuid")
    public String uuid;

    @Column(name = "task")
    public String task;

    @Column(name = "completed_at")
    public Date completedAt;
}
