package com.doerapispring.storage;

import com.doerapispring.domain.AbnormalModelException;
import com.doerapispring.domain.DomainRepository;
import com.doerapispring.domain.ScheduledFor;
import com.doerapispring.domain.Todo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Date;

@Repository
class TodoRepository implements DomainRepository<Todo, String> {
    private final UserDAO userDao;
    private final TodoDao todoDao;

    @Autowired
    TodoRepository(UserDAO userDao, TodoDao todoDao) {
        this.userDao = userDao;
        this.todoDao = todoDao;
    }

    @Override
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
}
