package com.doerapispring.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface SessionTokenDAO extends JpaRepository<SessionTokenEntity, Long> {
    SessionTokenEntity findByToken(String token);
}
