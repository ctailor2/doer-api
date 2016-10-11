package com.doerapispring.apiTokens;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Date;

/**
 * Created by chiragtailor on 8/22/16.
 */
public interface SessionTokenRepository extends JpaRepository<SessionTokenEntity, Long> {
    @Query("SELECT st FROM SessionTokenEntity st INNER JOIN st.userEntity u WHERE u.email = ?1 AND st.expiresAt > NOW()")
    SessionTokenEntity findActiveByUserEmail(String email);

    SessionTokenEntity findFirstByTokenAndExpiresAtAfter(String token, Date date);
}
