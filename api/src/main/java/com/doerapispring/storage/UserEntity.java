package com.doerapispring.storage;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDefaultListId() {
        return defaultListId;
    }

    public void setDefaultListId(String defaultListId) {
        this.defaultListId = defaultListId;
    }

    public String getPasswordDigest() {
        return passwordDigest;
    }

    public void setPasswordDigest(String passwordDigest) {
        this.passwordDigest = passwordDigest;
    }

    public Date getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Date createdAt) {
        this.createdAt = createdAt;
    }

    public Date getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(Date updatedAt) {
        this.updatedAt = updatedAt;
    }

    public UserEntity() {
    }

    public UserEntity(String email, String defaultListId, String passwordDigest, Date createdAt, Date updatedAt) {
        this.email = email;
        this.defaultListId = defaultListId;
        this.passwordDigest = passwordDigest;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
