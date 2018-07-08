package com.doerapispring.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

interface MasterListDao extends JpaRepository<MasterListEntity, Long> {
    @Query("SELECT ml FROM MasterListEntity ml " +
        "LEFT JOIN FETCH ml.todoEntities t " +
        "WHERE ml.email = ?1 " +
        "ORDER BY t.position")
    MasterListEntity findByEmail(String email);
}
