package com.doerapispring.storage;

import com.doerapispring.domain.ListId;
import com.doerapispring.domain.OwnedObjectRepository;
import com.doerapispring.domain.TodoList;
import com.doerapispring.domain.UserId;
import org.springframework.stereotype.Repository;
import org.springframework.util.IdGenerator;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class TodoListRepository implements OwnedObjectRepository<TodoList, UserId, ListId> {
    private final UserDAO userDAO;
    private final TodoListDao todoListDao;
    private IdGenerator idGenerator;

    TodoListRepository(UserDAO userDAO,
                       TodoListDao todoListDao,
                       IdGenerator idGenerator) {
        this.userDAO = userDAO;
        this.todoListDao = todoListDao;
        this.idGenerator = idGenerator;
    }

    @Override
    public void save(TodoList todoList) {
        UserEntity userEntity = userDAO.findByEmail(todoList.getUserId().get());
        TodoListEntity todoListEntity = TodoListEntity.builder()
            .userEntity(userEntity)
            .uuid(todoList.getListId().get())
            .name(todoList.getName())
            .build();
        todoListDao.save(todoListEntity);
    }

    @Override
    public ListId nextIdentifier() {
        return new ListId(idGenerator.generateId().toString());
    }

    @Override
    public List<TodoList> findAll(UserId userId) {
        return todoListDao.findByEmailWithoutTodos(userId.get()).stream()
            .map(todoListEntity -> new TodoList(
                userId,
                new ListId(todoListEntity.uuid),
                todoListEntity.name))
            .collect(Collectors.toList());
    }
}
