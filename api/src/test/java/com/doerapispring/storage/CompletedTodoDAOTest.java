package com.doerapispring.storage;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@RunWith(SpringRunner.class)
@ActiveProfiles(value = "test")
public class CompletedTodoDAOTest {
    @Autowired
    private
    CompletedTodoDAO completedTodoDAO;

    @Autowired
    private
    UserDAO userDAO;

    @Autowired
    private
    TodoListDao todoListDao;

    private CompletedTodoEntity.CompletedTodoEntityBuilder baseCompletedTodoBuilder;

    private Long userId;

    @Before
    public void setUp() throws Exception {
        UserEntity savedUserEntity = userDAO.save(
            UserEntity.builder()
                .email("some@email.com")
                .passwordDigest("somePasswordDigest")
                .createdAt(new Date())
                .updatedAt(new Date())
                .build());
        userId = savedUserEntity.id;
        String listId = "listId";
        todoListDao.save(TodoListEntity.builder()
            .uuid(listId)
            .name("someName")
            .userEntity(savedUserEntity)
            .lastUnlockedAt(new Date())
            .demarcationIndex(1)
            .build());
        baseCompletedTodoBuilder = CompletedTodoEntity.builder()
            .userId(savedUserEntity.id)
            .task("some task")
            .listId(listId);
    }

    @Test
    public void shouldGetCompletedTodosInDescendingOrderByCompletedAt() {
        CompletedTodoEntity olderCompletedTodo = baseCompletedTodoBuilder
            .completedAt(new Date(1800000))
            .build().withUuid("firstId");
        CompletedTodoEntity newerCompletedTodo = baseCompletedTodoBuilder
            .completedAt(new Date(3600000))
            .build().withUuid("secondId");
        completedTodoDAO.save(olderCompletedTodo);
        completedTodoDAO.save(newerCompletedTodo);

        assertThat(completedTodoDAO.count()).isEqualTo(2);
        assertThat(completedTodoDAO.findByUserIdOrderByCompletedAtDesc(userId).stream()
            .map(completedTodoEntity -> completedTodoEntity.id))
            .containsExactly(
                newerCompletedTodo.id,
                olderCompletedTodo.id);
    }
}