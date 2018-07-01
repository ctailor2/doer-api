package com.doerapispring.storage;

import org.springframework.data.jpa.repository.JpaRepository;

interface MasterListDao extends JpaRepository<MasterListEntity, Long> {
    MasterListEntity findByEmail(String email);
}
