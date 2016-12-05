package com.doerapispring.todos;

import com.doerapispring.AbnormalModelException;
import com.doerapispring.users.UserDAO;
import com.doerapispring.users.UserEntity;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;

/**
 * Created by chiragtailor on 11/24/16.
 */
@Repository
public class NewTodoRepository {
    private final UserDAO userDao;
    private final TodoDao todoDao;

    @Autowired
    public NewTodoRepository(UserDAO userDao, TodoDao todoDao) {
        this.userDao = userDao;
        this.todoDao = todoDao;
    }

    public void add(NewTodo todo) throws AbnormalModelException {
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
}
