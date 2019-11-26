package com.doerapispring.storage;

import com.doerapispring.domain.ListId;
import com.doerapispring.domain.ObjectRepository;
import com.doerapispring.domain.User;
import com.doerapispring.domain.UserId;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@ActiveProfiles(value = "test")
@RunWith(SpringRunner.class)
public class UserRepositoryTest {
    private ObjectRepository<User, UserId> userRepository;

    @Autowired
    private UserDAO userDAO;

    private final ArgumentCaptor<UserEntity> userEntityArgumentCaptor = ArgumentCaptor.forClass(UserEntity.class);

    @Before
    public void setUp() throws Exception {
        userRepository = new UserRepository(userDAO);
    }

    @Test
    public void savesUser() {
        UserId userId = new UserId("soUnique");
        User user = new User(userId, new ListId("someListId"));
        userRepository.save(user);

        assertThat(userRepository.find(userId)).contains(user);
    }

    @Test
    public void find_returnsAnEmptyOptionalWhenUserNotFound() {
        Optional<User> userOptional = userRepository.find(new UserId("doesNotExist"));

        assertThat(userOptional).isEmpty();
    }
}