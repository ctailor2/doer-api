package com.doerapispring.apiTokens;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Date;

/**
 * Created by chiragtailor on 8/22/16.
 */
public interface SessionTokenRepository extends JpaRepository<SessionToken, Long> {
    SessionToken findFirstByUserIdAndExpiresAtAfter(Long userId, Date date);

    SessionToken findFirstByTokenAndExpiresAtAfter(String token, Date date);
}
