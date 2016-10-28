package com.doerapispring.apiTokens;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

/**
 * Created by chiragtailor on 10/26/16.
 */
@Component
public interface SessionTokenDAO extends JpaRepository<SessionTokenEntity, Long> {
}
