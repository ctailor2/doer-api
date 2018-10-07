package com.doerapispring.storage;

import com.doerapispring.domain.*;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Repository
class ReadOnlyMasterListRepository implements
    ObjectRepository<ReadOnlyMasterList, String> {
    private final MasterListDao masterListDao;
    private final Clock clock;

    ReadOnlyMasterListRepository(
        MasterListDao masterListDao,
        Clock clock) {
        this.masterListDao = masterListDao;
        this.clock = clock;
    }

    @Override
    public Optional<ReadOnlyMasterList> find(UniqueIdentifier<String> uniqueIdentifier) {
        MasterListEntity masterListEntity = masterListDao.findByEmail(uniqueIdentifier.get());
        List<Todo> todos = masterListEntity.todoEntities.stream()
            .map(todoEntity -> new Todo(
                new TodoId(todoEntity.uuid),
                todoEntity.task))
            .collect(toList());
        return Optional.of(new ReadOnlyMasterList(clock, uniqueIdentifier, masterListEntity.lastUnlockedAt, todos, masterListEntity.demarcationIndex));
    }
}
