package com.doerapispring.storage;

import com.doerapispring.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.*;
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

    // TODO: Move away from using db primary key for identifier
    @Override
    public Optional<MasterList> find(UniqueIdentifier<String> uniqueIdentifier) {
        String email = uniqueIdentifier.get();
        List<TodoEntity> todoEntities = todoDao.findUnfinishedByUserEmail(email);
        Map<Boolean, List<Todo>> partitionedTodos = todoEntities.stream()
                .map(todoEntity -> new Todo(
                        todoEntity.id.toString(),
                        todoEntity.task,
                        todoEntity.active ? ScheduledFor.now : ScheduledFor.later,
                        todoEntity.position))
                .collect(Collectors.partitioningBy(todo -> ScheduledFor.now.equals(todo.getScheduling())));
        TodoList laterList = new TodoList(ScheduledFor.later, partitionedTodos.get(false), -1);
        TodoList nowList = new TodoList(ScheduledFor.now, partitionedTodos.get(true), 2, laterList);
        MasterList masterList = new MasterList(uniqueIdentifier, nowList, laterList);
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
        TodoEntity todoEntity = todoDao.findUserTodo(
                masterList.getIdentifier().get(),
                Long.valueOf(todo.getLocalIdentifier()));
        if (todoEntity == null) throw new AbnormalModelException();
        todoDao.delete(todoEntity);
    }

    @Override
    public void update(MasterList masterList, Todo todo) throws AbnormalModelException {
        TodoEntity todoEntity = todoDao.findUserTodo(
                masterList.getIdentifier().get(),
                Long.valueOf(todo.getLocalIdentifier()));
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


    @Override
    public void update(MasterList masterList, List<Todo> todos) throws AbnormalModelException {
        List<TodoEntity> todoEntities = todos.stream()
                .map(todo -> {
                    TodoEntity todoEntity = todoDao.findUserTodo(
                            masterList.getIdentifier().get(),
                            Long.valueOf(todo.getLocalIdentifier()));
                    if (todoEntity == null) return null;
                    return TodoEntity.builder()
                            .id(todoEntity.id)
                            .userEntity(todoEntity.userEntity)
                            .task(todo.getTask())
                            .active(ScheduledFor.now.equals(todo.getScheduling()))
                            .position(todo.getPosition())
                            .completed(todo.isComplete())
                            .createdAt(todoEntity.createdAt)
                            .updatedAt(new Date())
                            .build();
                })
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        if(todoEntities.size() < todos.size()) throw new AbnormalModelException();
        todoDao.save(todoEntities);
    }
}
