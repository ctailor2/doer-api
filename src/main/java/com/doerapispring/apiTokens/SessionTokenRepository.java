package com.doerapispring.apiTokens;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;

/**
 * Created by chiragtailor on 8/22/16.
 */
public interface SessionTokenRepository extends JpaRepository<SessionToken, Long> {
    @Query("SELECT st FROM SessionToken st INNER JOIN st.user u WHERE u.email = ?1 AND st.expiresAt > NOW()")
    SessionToken findActiveByUserEmail(String email);

    SessionToken findFirstByTokenAndExpiresAtAfter(String token, Date date);
}
