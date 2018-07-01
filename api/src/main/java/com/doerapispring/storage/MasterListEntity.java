package com.doerapispring.storage;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
@EqualsAndHashCode
@ToString
class MasterListEntity implements Serializable {
    @Column(name = "id")
    public Long id;

    @Id
    @Column(name = "email", unique = true)
    public String email;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id", referencedColumnName = "id")
    public List<TodoEntity> todoEntities = new ArrayList<>();

    @Column(name = "last_unlocked_at")
    public Date lastUnlockedAt;

    public List<TodoEntity> getTodoEntities() {
        return todoEntities;
    }
}
