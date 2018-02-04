package com.doerapispring.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "todos")
class TodoEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    @Column(name = "uuid")
    public String uuid;

    @Column(name = "task")
    public String task;

    @Column(name = "active")
    public boolean active;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "updated_at")
    public Date updatedAt;

    @Column(name = "completed")
    public boolean completed;

    @Column(name = "position")
    public Integer position;

    @ManyToOne
    @JoinColumn(name = "user_id")
    public UserEntity userEntity;
}
