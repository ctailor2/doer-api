package com.doerapispring.todos;

import com.doerapispring.AbnormalModelException;
import com.doerapispring.UserIdentifier;
import com.doerapispring.users.UserDAO;
import com.doerapispring.users.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by chiragtailor on 11/24/16.
 */
@Repository
public class TodoRepository {
    private final UserDAO userDao;
    private final TodoDao todoDao;

    @Autowired
    public TodoRepository(UserDAO userDao, TodoDao todoDao) {
        this.userDao = userDao;
        this.todoDao = todoDao;
    }

    public void add(Todo todo) throws AbnormalModelException {
        UserEntity userEntity = userDao.findByEmail(todo.getUserIdentifier().get());
        if (userEntity == null) throw new AbnormalModelException();
        TodoEntity todoEntity = TodoEntity.builder()
                .userEntity(userEntity)
                .task(todo.getTask())
                .active(todo.getScheduling() == ScheduledFor.now)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        todoDao.save(todoEntity);
    }

    public List<Todo> find(UserIdentifier userIdentifier) {
        List<TodoEntity> todoEntities = todoDao.findByUserEmail(userIdentifier.get());
        return todoEntities.stream().map(todoEntity ->
                mapTodoEntityToTodo(userIdentifier, todoEntity))
                .collect(Collectors.toList());
    }

    public List<Todo> findByScheduling(UserIdentifier userIdentifier, ScheduledFor scheduling) {
        if (scheduling.equals(ScheduledFor.anytime)) {
            return find(userIdentifier);
        }
        List<TodoEntity> todoEntities = todoDao.findByUserEmailAndActiveStatus(userIdentifier.get(), scheduling.equals(ScheduledFor.now));
        return todoEntities.stream().map(todoEntity ->
                mapTodoEntityToTodo(userIdentifier, todoEntity))
                .collect(Collectors.toList());
    }

    private Todo mapTodoEntityToTodo(UserIdentifier userIdentifier, TodoEntity todoEntity) {
        return new Todo(userIdentifier, todoEntity.task,
                todoEntity.active ? ScheduledFor.now : ScheduledFor.later);
    }
}
