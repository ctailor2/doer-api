package com.doerapispring.users;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
public interface UserDAO extends JpaRepository<UserEntity, Long> {
    UserEntity findByEmail(String email);
}
