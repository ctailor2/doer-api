package com.doerapispring.apiTokens;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * Created by chiragtailor on 8/22/16.
 */
public interface SessionTokenRepository extends JpaRepository<SessionToken, Long> {
    List<SessionToken> findByUserId(Long userId);
}
