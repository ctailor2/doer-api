package com.doerapispring.users;

import lombok.Builder;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by chiragtailor on 8/9/16.
 */
@Builder
@Entity
@Table(name = "users")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    @Column(name = "email", unique = true)
    public String email;

    @Column(name = "password_digest")
    public String password_digest;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "updated_at")
    public Date updatedAt;
}
