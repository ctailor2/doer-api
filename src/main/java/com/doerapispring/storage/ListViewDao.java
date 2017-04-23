package com.doerapispring.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
interface ListViewDao extends JpaRepository<ListViewEntity, Long> {
    @Query("SELECT lv FROM ListViewEntity lv " +
            "INNER JOIN lv.userEntity u " +
            "WHERE u.email = ?1 " +
            "ORDER BY lv.updatedAt DESC")
    List<ListViewEntity> findUserListView(String email);
}
