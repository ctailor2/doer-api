package com.doerapispring.storage;

import com.doerapispring.domain.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.sql.Date;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@RunWith(SpringRunner.class)
@ActiveProfiles(value = "test")
public class CompletedTodoRepositoryTest {
    @Autowired
    private CompletedTodoDAO completedTodoDAO;

    @Autowired
    private UserDAO userDAO;

    private UserRepository userRepository;

    private CompletedTodoRepository completedTodoRepository;

    @Before
    public void setUp() throws Exception {
        completedTodoRepository = new CompletedTodoRepository(completedTodoDAO, userDAO);
        userRepository = new UserRepository(userDAO);

    }

    @Test
    public void savesCompletedTodos() throws AbnormalModelException {
        UserId userId = new UserId("someUserId");
        userRepository.save(new User(userId));

        CompletedTodo completedTodo = new CompletedTodo(
            userId,
            new CompletedTodoId("someCompletedTodoId"),
            "someTask",
            Date.from(Instant.now()));
        completedTodoRepository.save(completedTodo);

        List<CompletedTodo> completedTodos = completedTodoRepository.findAll(userId);
        assertThat(completedTodos).contains(completedTodo);
    }
}