package com.doerapispring.users;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * Created by chiragtailor on 8/9/16.
 */
@Entity
@Table(name = "users")
public class User {
    @Id
    @Column(name = "id")
    public Long id;

    @Column(name = "email", unique = true)
    public String email;
}
