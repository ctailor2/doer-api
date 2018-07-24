package com.doerapispring.storage;

import com.doerapispring.domain.MasterList;
import com.doerapispring.domain.ObjectRepository;
import com.doerapispring.domain.UniqueIdentifier;
import com.doerapispring.domain.User;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.test.context.junit4.SpringRunner;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.BEFORE_TEST_METHOD)
@Sql(scripts = "/cleanup.sql", executionPhase = Sql.ExecutionPhase.AFTER_TEST_METHOD)
@ActiveProfiles(value = "test")
@RunWith(SpringRunner.class)
public class MasterListRepositoryTest {
    private ObjectRepository<MasterList, String> masterListRepository;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Autowired
    private MasterListDao masterListDao;

    @Autowired
    private UserDAO userDao;

    private Clock clock;
    private UserRepository userRepository;

    @Before
    public void setUp() throws Exception {
        clock = Clock.systemDefaultZone();
        masterListRepository = new MasterListRepository(userDao, masterListDao, clock);
        userRepository = new UserRepository(userDao);
    }

    @Test
    public void savesMasterList() throws Exception {
        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>("someIdentifier");
        userRepository.add(new User(uniqueIdentifier));
        MasterList masterList = new MasterList(clock, uniqueIdentifier, Date.from(Instant.parse("2007-12-03T10:15:30.00Z")), new ArrayList<>(), 0);
        masterList.addDeferred("firstTask");
        masterList.add("secondTask");
        masterList.addDeferred("thirdTask");
        masterList.add("fourthTask");
        masterList.unlock();

        masterListRepository.save(masterList);

        Optional<MasterList> masterListOptional = masterListRepository.find(uniqueIdentifier);

        MasterList retrievedMasterList = masterListOptional.get();
        assertThat(retrievedMasterList).isEqualToIgnoringGivenFields(masterList, "lastUnlockedAt");
//        Have to 're-wrap' this into a date for the assertion because it comes back from the db as a java.sql.Timestamp
        assertThat(Date.from(retrievedMasterList.getLastUnlockedAt().toInstant())).isCloseTo(masterList.getLastUnlockedAt(), 10L);
    }
}