package com.doerapispring.storage;

import com.doerapispring.domain.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.IdGenerator;

import java.sql.Date;
import java.time.Clock;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Repository
class TodoListRepository implements
    OwnedObjectRepository<TodoList, UserId, ListId> {
    private final UserDAO userDAO;
    private final TodoListDao todoListDao;
    private final Clock clock;
    private final IdGenerator idGenerator;

    TodoListRepository(
        UserDAO userDAO,
        TodoListDao todoListDao,
        Clock clock,
        IdGenerator idGenerator) {
        this.userDAO = userDAO;
        this.todoListDao = todoListDao;
        this.clock = clock;
        this.idGenerator = idGenerator;
    }

    @Override
    public void save(TodoList todoList) throws AbnormalModelException {
        UserEntity userEntity = userDAO.findByEmail(todoList.getUserId().get());
        if (userEntity == null) throw new AbnormalModelException();
        TodoListEntity todoListEntity = new TodoListEntity();
        todoListEntity.userEntity = userEntity;
        todoListEntity.name = todoList.getName();
        todoListEntity.uuid = todoList.getListId().get();
        todoListEntity.demarcationIndex = todoList.getDemarcationIndex();
        List<Todo> allTodos = todoList.getAllTodos();
        for (int i = 0; i < allTodos.size(); i++) {
            Todo todo = allTodos.get(i);
            todoListEntity.todoEntities.add(
                TodoEntity.builder()
                    .uuid(todo.getTodoId().getIdentifier())
                    .task(todo.getTask())
                    .position(i)
                    .build());
        }
        todoListEntity.lastUnlockedAt = todoList.getLastUnlockedAt();
        todoListDao.save(todoListEntity);
    }

    @Override
    public Optional<TodoList> find(UserId userId, ListId listId) {
        TodoListEntity todoListEntity = todoListDao.findByEmailAndListId(userId.get(), listId.get());
        return Optional.of(mapToTodoList(userId, todoListEntity));
    }

    @Override
    public List<TodoList> findAll(UserId userId) {
        List<TodoListEntity> todoListEntities = todoListDao.findByEmail(userId.get());
        return todoListEntities.stream()
            .map(todoListEntity -> mapToTodoList(userId, todoListEntity))
            .collect(toList());
    }

    @Override
    public ListId nextIdentifier() {
        return new ListId(idGenerator.generateId().toString());
    }

    private TodoList mapToTodoList(UserId userId, TodoListEntity todoListEntity) {
        List<Todo> todos = todoListEntity.todoEntities.stream()
            .map(todoEntity -> new Todo(
                new TodoId(todoEntity.uuid),
                todoEntity.task))
            .collect(toList());
        return new TodoList(
            clock,
            userId,
            new ListId(todoListEntity.uuid),
            todoListEntity.name,
            Date.from(todoListEntity.lastUnlockedAt.toInstant()),
            todos,
            todoListEntity.demarcationIndex);
    }
}
