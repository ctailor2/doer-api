package com.doerapispring.todos;

import com.doerapispring.users.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by chiragtailor on 9/27/16.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "todos")
public class Todo {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    //TODO: refactor this away, since the many to one was added
    @Column(name = "user_id", insertable = false, updatable = false)
    public Long userId;

    @Column(name = "task")
    public String task;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "updated_at")
    public Date updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    public User user;
}
