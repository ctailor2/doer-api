package com.doerapispring.storage;

import com.doerapispring.domain.*;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
        Map<Boolean, List<Todo>> partitionedTodos = masterListEntity.todoEntities.stream()
            .map(todoEntity -> new Todo(
                todoEntity.uuid,
                todoEntity.task,
                todoEntity.active ? MasterList.NAME : MasterList.DEFERRED_NAME,
                todoEntity.position))
            .collect(Collectors.partitioningBy(todo -> MasterList.NAME.equals(todo.getListName())));
        return Optional.of(new MasterList(clock, uniqueIdentifier, partitionedTodos.get(true), partitionedTodos.get(false), masterListEntity.lastUnlockedAt));
    }

    @Override
    public void save(MasterList masterList) throws AbnormalModelException {
        UserEntity userEntity = userDAO.findByEmail(masterList.getIdentifier().get());
        if (userEntity == null) throw new AbnormalModelException();
        MasterListEntity masterListEntity = new MasterListEntity();
        masterListEntity.id = userEntity.id;
        masterListEntity.email = masterList.getIdentifier().get();
        List<Todo> allTodos = new ArrayList<>();
        allTodos.addAll(masterList.getAllTodos());
        masterListEntity.todoEntities.addAll(allTodos
            .stream()
            .map(todo -> TodoEntity.builder()
                .userEntity(userEntity)
                .uuid(todo.getLocalIdentifier())
                .task(todo.getTask())
                .active(MasterList.NAME.equals(todo.getListName()))
                .position(todo.getPosition())
                .build())
            .collect(toList()));
        masterListEntity.lastUnlockedAt = masterList.getLastUnlockedAt();
        masterListDao.save(masterListEntity);
    }
}
