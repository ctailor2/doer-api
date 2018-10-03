package com.doerapispring.storage;

import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.util.Date;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Entity
@Table(name = "todos")
@EntityListeners(AuditingEntityListener.class)
class TodoEntity {
    @Id
    @Column(name = "uuid")
    public String uuid;

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
