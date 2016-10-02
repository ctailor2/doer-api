package com.doerapispring.apiTokens;

import com.doerapispring.users.User;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by chiragtailor on 8/22/16.
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "session_tokens")
public class SessionToken {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    public Long id;

    @Column(name = "token")
    public String token;

    @Column(name = "expires_at")
    public Date expiresAt;

    @Column(name = "created_at")
    public Date createdAt;

    @Column(name = "updated_at")
    public Date updatedAt;

    @ManyToOne
    @JoinColumn(name = "user_id")
    public User user;
}