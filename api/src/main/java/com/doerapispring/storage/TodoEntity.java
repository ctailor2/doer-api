package com.doerapispring.storage;

import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@NoArgsConstructor
@EqualsAndHashCode
@ToString
@Entity
@Table(name = "todos")
@EntityListeners(AuditingEntityListener.class)
class TodoEntity {
    @Builder
    private TodoEntity(String task, Date createdAt, Date updatedAt, Integer position) {
        this.task = task;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
        this.position = position;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "task")
    public String task;

    @Column(name = "created_at", updatable = false)
    @CreatedDate
    public Date createdAt;

    @Column(name = "updated_at")
    @LastModifiedDate
    public Date updatedAt;

    @Column(name = "position")
    public Integer position;
}
