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
        todoEntities.stream().forEach(todoEntity -> {
                    try {
                        masterList.add(todoEntity.task,
                                todoEntity.active ? ScheduledFor.now : ScheduledFor.later);
                    } catch (ListSizeExceededException | DuplicateTodoException e) {
                        // TODO: This shouldn't happen if the rules of the domain are enforced
                        // when objects are added to the repository. Think about what to do here.
                        e.printStackTrace();
                    }
                }
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

    @Override
    public void remove(MasterList masterList, Todo todo) throws AbnormalModelException {
        TodoEntity todoEntity = todoDao.findUnfinished(
                masterList.getIdentifier().get(),
                todo.getTask(),
                ScheduledFor.now.equals(todo.getScheduling()));
        if (todoEntity == null) throw new AbnormalModelException();
        todoDao.delete(todoEntity);
    }
}
