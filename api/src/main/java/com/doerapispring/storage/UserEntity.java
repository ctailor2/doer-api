package com.doerapispring.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "users")
class UserEntity {
    @Id
    @Column(name = "email", unique = true)
    public String email;

    @Column(name = "default_list_id")
    public String defaultListId;

    @Column(name = "passwordDigest")
    public String passwordDigest;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "updated_at")
    public Date updatedAt;
}
