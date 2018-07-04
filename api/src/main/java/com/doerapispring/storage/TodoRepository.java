package com.doerapispring.storage;

import com.doerapispring.domain.AbnormalModelException;
import com.doerapispring.domain.AggregateRootRepository;
import com.doerapispring.domain.MasterList;
import com.doerapispring.domain.Todo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Repository
class TodoRepository implements AggregateRootRepository<MasterList, Todo> {
    private final UserDAO userDAO;
    private final TodoDao todoDao;

    @Autowired
    TodoRepository(UserDAO userDAO, TodoDao todoDao) {
        this.userDAO = userDAO;
        this.todoDao = todoDao;
    }

    @Override
    public void add(MasterList masterList, Todo todo) throws AbnormalModelException {
        UserEntity userEntity = userDAO.findByEmail(masterList.getIdentifier().get());
        if (userEntity == null) throw new AbnormalModelException();
        todoDao.save(TodoEntity.builder()
                .uuid(todo.getLocalIdentifier())
                .userEntity(userEntity)
                .task(todo.getTask())
                .active(MasterList.NAME.equals(todo.getListName()))
                .position(todo.getPosition())
                .createdAt(new Date())
                .updatedAt(new Date())
                .build());
    }

    @Override
    public void remove(MasterList masterList, Todo todo) throws AbnormalModelException {
        TodoEntity todoEntity = todoDao.findUserTodo(
                masterList.getIdentifier().get(),
                todo.getLocalIdentifier());
        if (todoEntity == null) throw new AbnormalModelException();
        todoDao.delete(todoEntity);
    }

    @Override
    public void update(MasterList masterList, Todo todo) throws AbnormalModelException {
        TodoEntity todoEntity = todoDao.findUserTodo(
                masterList.getIdentifier().get(),
                todo.getLocalIdentifier());
        if (todoEntity == null) throw new AbnormalModelException();
        todoDao.save(TodoEntity.builder()
                .uuid(todoEntity.uuid)
                .userEntity(todoEntity.userEntity)
                .task(todo.getTask())
                .active(MasterList.NAME.equals(todo.getListName()))
                .position(todo.getPosition())
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
                            todo.getLocalIdentifier());
                    if (todoEntity == null) return null;
                    return TodoEntity.builder()
                            .uuid(todoEntity.uuid)
                            .userEntity(todoEntity.userEntity)
                            .task(todo.getTask())
                            .active(MasterList.NAME.equals(todo.getListName()))
                            .position(todo.getPosition())
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
