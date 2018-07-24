package com.doerapispring.storage;

import com.doerapispring.domain.*;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Repository
class MasterListRepository implements ObjectRepository<MasterList, String> {
    private final UserDAO userDAO;
    private final MasterListDao masterListDao;
    private final Clock clock;

    MasterListRepository(
        UserDAO userDAO,
        MasterListDao masterListDao,
        Clock clock) {
        this.userDAO = userDAO;
        this.masterListDao = masterListDao;
        this.clock = clock;
    }

    @Override
    public Optional<MasterList> find(UniqueIdentifier<String> uniqueIdentifier) {
        MasterListEntity masterListEntity = masterListDao.findByEmail(uniqueIdentifier.get());
        List<Todo> todos = masterListEntity.todoEntities.stream()
            .map(todoEntity -> new Todo(
                todoEntity.uuid,
                todoEntity.task,
                todoEntity.position))
            .collect(toList());
        return Optional.of(new MasterList(clock, uniqueIdentifier, masterListEntity.lastUnlockedAt, todos, masterListEntity.demarcationIndex));
    }

    @Override
    public void save(MasterList masterList) throws AbnormalModelException {
        UserEntity userEntity = userDAO.findByEmail(masterList.getIdentifier().get());
        if (userEntity == null) throw new AbnormalModelException();
        MasterListEntity masterListEntity = new MasterListEntity();
        masterListEntity.id = userEntity.id;
        masterListEntity.email = masterList.getIdentifier().get();
        masterListEntity.demarcationIndex = masterList.getDemarcationIndex();
        List<Todo> allTodos = new ArrayList<>();
        allTodos.addAll(masterList.getAllTodos());
        masterListEntity.todoEntities.addAll(allTodos
            .stream()
            .map(todo -> TodoEntity.builder()
                .uuid(todo.getLocalIdentifier())
                .task(todo.getTask())
                .position(todo.getPosition())
                .build())
            .collect(toList()));
        masterListEntity.lastUnlockedAt = masterList.getLastUnlockedAt();
        masterListDao.save(masterListEntity);
    }
}
