package com.doerapispring.storage;

import org.springframework.data.jpa.repository.JpaRepository;

interface CompletedListDAO extends JpaRepository<CompletedListEntity, Long> {
    CompletedListEntity findByEmail(String email);
}
