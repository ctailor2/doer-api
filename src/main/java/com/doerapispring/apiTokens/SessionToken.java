package com.doerapispring.apiTokens;

import lombok.Builder;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by chiragtailor on 8/22/16.
 */
@Builder
@Entity
@Table(name = "session_tokens")
class SessionToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    @Column(name = "user_id")
    public Long userId;

    @Column(name = "token")
    public String token;

    @Column(name = "expires_at")
    public Date expiresAt;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "updated_at")
    public Date updatedAt;
}
