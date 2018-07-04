package com.doerapispring.storage;

import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Entity
@Table(name = "users")
class CompletedListEntity implements Serializable {
    @Id
    @Column(name = "id")
    public Long id;

    @Column(name = "email", unique = true)
    public String email;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "user_id", referencedColumnName = "id", nullable = false)
    public List<CompletedTodoEntity> completedTodoEntities = new ArrayList<>();
}
