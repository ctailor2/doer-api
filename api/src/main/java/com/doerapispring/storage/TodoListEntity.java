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
@Table(name = "lists")
@EqualsAndHashCode
@ToString
public class TodoListEntity implements Serializable {
    @Id
    @Column(name = "uuid")
    public String uuid;

    @Column(name = "name")
    public String name;

    @ManyToOne
    @JoinColumn(name = "user_identifier", nullable = false)
    public UserEntity userEntity;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JoinColumn(name = "list_id", referencedColumnName = "uuid", nullable = false)
    public List<TodoEntity> todoEntities = new ArrayList<>();

    @Column(name = "last_unlocked_at")
    public Date lastUnlockedAt;

    @Column(name = "demarcation_index")
    public Integer demarcationIndex;
}
