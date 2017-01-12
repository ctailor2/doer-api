package com.doerapispring.storage;

import com.doerapispring.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Repository
class MasterListRepository implements AggregateRootRepository<MasterList, Todo, String> {
    private final UserDAO userDAO;
    private final TodoDao todoDao;

    @Autowired
    MasterListRepository(UserDAO userDAO, TodoDao todoDao) {
        this.userDAO = userDAO;
        this.todoDao = todoDao;
    }

    @Override
    public Optional<MasterList> find(UniqueIdentifier<String> uniqueIdentifier) {
        String email = uniqueIdentifier.get();
        List<TodoEntity> todoEntities = todoDao.findUnfinishedByUserEmail(email);
        MasterList masterList = MasterList.newEmpty(uniqueIdentifier);
        todoEntities.stream().forEach(todoEntity ->
                masterList.add(todoEntity.task,
                        todoEntity.active ? ScheduledFor.now : ScheduledFor.later)
        );
        return Optional.of(masterList);
    }

    @Override
    public void add(MasterList masterList, Todo todo) throws AbnormalModelException {
        UserEntity userEntity = userDAO.findByEmail(masterList.getIdentifier().get());
        if (userEntity == null) throw new AbnormalModelException();
        todoDao.save(TodoEntity.builder()
                .userEntity(userEntity)
                .task(todo.getTask())
                .active(ScheduledFor.now.equals(todo.getScheduling()))
                .createdAt(new Date())
                .updatedAt(new Date())
                .build());
    }
}
