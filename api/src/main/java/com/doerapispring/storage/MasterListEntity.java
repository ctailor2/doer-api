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
public class MasterListEntity implements Serializable {
    @Id
    @Column(name = "id")
    public Long id;

    @Column(name = "email", unique = true)
    public String email;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    public List<TodoEntity> todoEntities = new ArrayList<>();

    @Column(name = "last_unlocked_at")
    public Date lastUnlockedAt;

    @Column(name = "demarcation_index")
    public Integer demarcationIndex;

    public List<TodoEntity> getTodoEntities() {
        return todoEntities;
    }
}
