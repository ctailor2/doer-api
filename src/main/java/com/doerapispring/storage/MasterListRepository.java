package com.doerapispring.storage;

import com.doerapispring.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

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
        List<Todo> todos = todoEntities.stream()
                .map(todoEntity -> new Todo(
                        todoEntity.task,
                        todoEntity.active ? ScheduledFor.now : ScheduledFor.later,
                        todoEntity.position))
                .collect(Collectors.toList());
        MasterList masterList = new MasterList(uniqueIdentifier, 2, todos);
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
                .position(todo.getPosition())
                .createdAt(new Date())
                .updatedAt(new Date())
                .build());
    }

    @Override
    public void remove(MasterList masterList, Todo todo) throws AbnormalModelException {
        TodoEntity todoEntity = todoDao.findUnfinishedInList(
                masterList.getIdentifier().get(),
                todo.getPosition(),
                ScheduledFor.now.equals(todo.getScheduling()));
        if (todoEntity == null) throw new AbnormalModelException();
        todoDao.delete(todoEntity);
    }

    @Override
    public void update(MasterList masterList, Todo todo) throws AbnormalModelException {
        TodoEntity todoEntity = todoDao.findUnfinishedInList(
                masterList.getIdentifier().get(),
                todo.getPosition(),
                ScheduledFor.now.equals(todo.getScheduling()));
        if (todoEntity == null) throw new AbnormalModelException();
        todoDao.save(TodoEntity.builder()
                .id(todoEntity.id)
                .userEntity(todoEntity.userEntity)
                .task(todo.getTask())
                .active(ScheduledFor.now.equals(todo.getScheduling()))
                .position(todo.getPosition())
                .completed(todo.isComplete())
                .createdAt(todoEntity.createdAt)
                .updatedAt(new Date())
                .build());
    }
}
