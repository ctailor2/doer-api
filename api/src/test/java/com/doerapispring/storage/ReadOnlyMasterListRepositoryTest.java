package com.doerapispring.storage;

import com.doerapispring.domain.*;
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
public class ReadOnlyMasterListRepositoryTest {
    private ObjectRepository<ReadOnlyMasterList, String> readOnlyMasterListRepository;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Autowired
    private MasterListDao masterListDao;

    @Autowired
    private ObjectRepository<MasterList, String> masterListRepository;

    @Autowired
    private ObjectRepository<User, String> userRepository;

    private Clock clock;

    @Before
    public void setUp() throws Exception {
        clock = Clock.systemDefaultZone();
        readOnlyMasterListRepository = new ReadOnlyMasterListRepository(masterListDao, clock);
    }

    @Test
    public void findsMasterList() throws Exception {
        UniqueIdentifier<String> uniqueIdentifier = new UniqueIdentifier<>("someIdentifier");
        userRepository.add(new User(uniqueIdentifier));
        MasterList masterList = new MasterList(clock, uniqueIdentifier, Date.from(Instant.parse("2007-12-03T10:15:30.00Z")), new ArrayList<>(), 0);
        masterList.addDeferred(new TodoId("1"), "firstTask");
        masterList.add(new TodoId("2"), "secondTask");
        masterList.addDeferred(new TodoId("3"), "thirdTask");
        masterList.add(new TodoId("4"), "fourthTask");
        masterList.unlock();

        masterListRepository.save(masterList);

        Optional<ReadOnlyMasterList> masterListOptional = readOnlyMasterListRepository.find(uniqueIdentifier);

        ReadOnlyMasterList retrievedMasterList = masterListOptional.get();
        assertThat(retrievedMasterList).isEqualToIgnoringGivenFields(masterList, "lastUnlockedAt");
    }
}