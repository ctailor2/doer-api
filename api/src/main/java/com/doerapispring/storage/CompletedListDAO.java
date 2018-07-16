package com.doerapispring.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface CompletedListDAO extends JpaRepository<CompletedListEntity, Long> {
    @Query("SELECT cl FROM CompletedListEntity cl " +
        "LEFT JOIN FETCH cl.completedTodoEntities t " +
        "WHERE cl.email = ?1 " +
        "ORDER BY t.completedAt DESC")
    CompletedListEntity findByEmail(String email);
}
