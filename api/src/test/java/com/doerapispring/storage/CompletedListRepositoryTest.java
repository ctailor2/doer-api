package com.doerapispring.storage;

import com.doerapispring.domain.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Clock;
import java.util.Date;
import java.util.Optional;

import static java.util.Collections.singletonList;
import static org.fest.assertions.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace.NONE;

@DataJpaTest
@AutoConfigureTestDatabase(replace = NONE)
@RunWith(SpringRunner.class)
@ActiveProfiles(value = "test")
public class CompletedListRepositoryTest {
    private ObjectRepository<CompletedList, String> completedListRepository;
    private UserRepository userRepository;

    @Autowired
    private UserDAO userDAO;

    @Autowired
    private CompletedListDAO completedListDAO;

    private Clock mockClock;

    @Before
    public void setUp() throws Exception {
        mockClock = mock(Clock.class);
        completedListRepository = new CompletedListRepository(mockClock, completedListDAO, userDAO);
        userRepository = new UserRepository(userDAO);
    }

    @Test
    public void savesCompletedList() throws Exception {
        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>("someIdentifier");
        userRepository.add(new User(uniqueIdentifier));
        CompletedList completedList = new CompletedList(mockClock, uniqueIdentifier, singletonList(
            new CompletedTodo("someTask", new Date())));

        completedListRepository.save(completedList);

        Optional<CompletedList> completedListOptional = completedListRepository.find(uniqueIdentifier);
        assertThat(completedListOptional.get()).isEqualTo(completedList);
    }
}