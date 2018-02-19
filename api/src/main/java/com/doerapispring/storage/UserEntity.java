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
@Table(name = "users")
class UserEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    @Column(name = "email", unique = true)
    public String email;

    @Column(name = "passwordDigest")
    public String passwordDigest;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "updated_at")
    public Date updatedAt;
}
