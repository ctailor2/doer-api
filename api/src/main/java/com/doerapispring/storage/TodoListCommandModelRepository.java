package com.doerapispring.storage;

import com.doerapispring.domain.*;

import java.sql.Date;
import java.time.Clock;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

class TodoListCommandModelRepository implements
        OwnedObjectRepository<TodoListCommandModel, UserId, ListId> {
    private final UserDAO userDAO;
    private final TodoListDao todoListDao;
    private final Clock clock;

    TodoListCommandModelRepository(
            UserDAO userDAO,
            TodoListDao todoListDao,
            Clock clock) {
        this.userDAO = userDAO;
        this.todoListDao = todoListDao;
        this.clock = clock;
    }

    @Override
    public void save(TodoListCommandModel todoListCommandModel) {
        UserEntity userEntity = userDAO.findByEmail(todoListCommandModel.getUserId().get());
        if (userEntity == null)
            throw new RuntimeException("TodoList must userId must correspond with an existing user");
        TodoListEntity todoListEntity = new TodoListEntity();
        todoListEntity.userEntity = userEntity;
        todoListEntity.name = todoListCommandModel.getName();
        todoListEntity.uuid = todoListCommandModel.getListId().get();
        todoListEntity.demarcationIndex = todoListCommandModel.getDemarcationIndex();
        List<Todo> allTodos = todoListCommandModel.getAllTodos();
        for (int i = 0; i < allTodos.size(); i++) {
            Todo todo = allTodos.get(i);
            todoListEntity.todoEntities.add(
                    TodoEntity.builder()
                            .uuid(todo.getTodoId().getIdentifier())
                            .task(todo.getTask())
                            .position(i)
                            .build());
        }
        todoListEntity.lastUnlockedAt = todoListCommandModel.getLastUnlockedAt();
        todoListDao.save(todoListEntity);
    }

    @Override
    public Optional<TodoListCommandModel> find(UserId userId, ListId listId) {
        TodoListEntity todoListEntity = todoListDao.findByEmailAndListId(userId.get(), listId.get());
        return Optional.of(mapToTodoList(userId, todoListEntity));
    }

    @Override
    public ListId nextIdentifier() {
        return null;
    }

    private TodoListCommandModel mapToTodoList(UserId userId, TodoListEntity todoListEntity) {
        List<Todo> todos = todoListEntity.todoEntities.stream()
                .map(todoEntity -> new Todo(
                        new TodoId(todoEntity.uuid),
                        todoEntity.task))
                .collect(toList());
        return new TodoListCommandModel(
                clock,
                userId,
                new ListId(todoListEntity.uuid),
                todoListEntity.name,
                Date.from(todoListEntity.lastUnlockedAt.toInstant()),
                todos,
                todoListEntity.demarcationIndex);
    }
}
