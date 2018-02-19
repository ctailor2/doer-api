package com.doerapispring.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

@Component
interface UserDAO extends JpaRepository<UserEntity, Long> {
    UserEntity findByEmail(String email);
}
