package com.doerapispring.storage;

import com.doerapispring.domain.*;
import org.springframework.stereotype.Repository;
import org.springframework.util.IdGenerator;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

@Repository
class TodoListRepository implements
    IdentityGeneratingObjectRepository<TodoList, String> {
    private final UserDAO userDAO;
    private final TodoListDao todoListDao;
    private final IdGenerator idGenerator;
    private final Clock clock;

    TodoListRepository(
        UserDAO userDAO,
        TodoListDao todoListDao,
        IdGenerator idGenerator,
        Clock clock) {
        this.userDAO = userDAO;
        this.todoListDao = todoListDao;
        this.idGenerator = idGenerator;
        this.clock = clock;
    }

    @Override
    public Optional<TodoList> find(UniqueIdentifier<String> uniqueIdentifier) {
        TodoListEntity todoListEntity = todoListDao.findByEmail(uniqueIdentifier.get());
        List<Todo> todos = todoListEntity.todoEntities.stream()
            .map(todoEntity -> new Todo(
                new TodoId(todoEntity.uuid),
                todoEntity.task))
            .collect(toList());
        return Optional.of(new TodoList(clock, uniqueIdentifier, todoListEntity.lastUnlockedAt, todos, todoListEntity.demarcationIndex));
    }

    @Override
    public void save(TodoList todoList) throws AbnormalModelException {
        UserEntity userEntity = userDAO.findByEmail(todoList.getIdentifier().get());
        if (userEntity == null) throw new AbnormalModelException();
        TodoListEntity todoListEntity = new TodoListEntity();
        todoListEntity.id = userEntity.id;
        todoListEntity.email = todoList.getIdentifier().get();
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
    public UniqueIdentifier<String> nextIdentifier() {
        return new UniqueIdentifier<>(idGenerator.generateId().toString());
    }
}
