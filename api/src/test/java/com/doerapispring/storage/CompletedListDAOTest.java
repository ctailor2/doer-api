package com.doerapispring.storage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@ActiveProfiles("test")
@RunWith(SpringRunner.class)
public class CompletedListDAOTest {
    @Autowired
    CompletedListDAO completedListDAO;

    @Autowired
    JdbcTemplate jdbcTemplate;

    private Long userId;

    @Before
    public void setUp() throws Exception {
        jdbcTemplate.update("INSERT INTO " +
            "users (email, password_digest, created_at, updated_at) " +
            "VALUES ('someEmail', 'somePasswordDigest', now(), now())");
        userId = jdbcTemplate.queryForObject("SELECT id FROM users WHERE email = 'someEmail'", Long.class);
    }

    @Test
    public void findsCompletedList_withTodosOrderedByCompletedAt_inDescendingOrder() {
        jdbcTemplate.update("INSERT INTO " +
            "completed_todos (user_id, uuid, task, completed_at) " +
            "VALUES (" + userId + ", 'uuid2', 'task2', now())");
        jdbcTemplate.update("INSERT INTO " +
            "completed_todos (user_id, uuid, task, completed_at) " +
            "VALUES (" + userId + ", 'uuid1', 'task1', now() + interval '1 hour')");

        CompletedListEntity completedListEntity = completedListDAO.findByEmail("someEmail");
        List<CompletedTodoEntity> completedTodoEntities = completedListEntity.getCompletedTodoEntities();
        assertThat(completedTodoEntities).hasSize(2);
        assertThat(completedTodoEntities.get(0).task).isEqualTo("task1");
        assertThat(completedTodoEntities.get(1).task).isEqualTo("task2");
    }
}