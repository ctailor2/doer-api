package com.doerapispring.storage;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "lists")
public class TodoListEntity implements Serializable {
    @Id
    @Column(name = "uuid")
    public String uuid;

    @Column(name = "name")
    public String name;

    @ManyToOne
    @JoinColumn(name = "user_identifier", nullable = false)
    public UserEntity userEntity;

    public TodoListEntity() {
    }

    public TodoListEntity(String uuid, String name, UserEntity userEntity) {
        this.uuid = uuid;
        this.name = name;
        this.userEntity = userEntity;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TodoListEntity that = (TodoListEntity) o;
        return Objects.equals(uuid, that.uuid) &&
                Objects.equals(name, that.name) &&
                Objects.equals(userEntity, that.userEntity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(uuid, name, userEntity);
    }
}
