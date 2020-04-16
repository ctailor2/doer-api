package com.doerapispring.domain;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TodoService implements TodoApplicationService {
    private final OwnedObjectRepository<TodoListCommandModel, UserId, ListId> todoListRepository;
    private final IdentityGeneratingRepository<TodoId> todoRepository;

    TodoService(OwnedObjectRepository<TodoListCommandModel, UserId, ListId> todoListRepository,
                IdentityGeneratingRepository<TodoId> todoRepository) {
        this.todoListRepository = todoListRepository;
        this.todoRepository = todoRepository;
    }

    public void create(User user, ListId listId, String task) {
        TodoListCommandModel todoListCommandModel = todoListRepository.find(user.getUserId(), listId)
                .orElseThrow(ListNotFoundException::new);
        TodoId todoId = todoRepository.nextIdentifier();
        todoListCommandModel.add(todoId, task);
        todoListRepository.save(todoListCommandModel);
    }

    public void createDeferred(User user, ListId listId, String task) {
        TodoListCommandModel todoListCommandModel = todoListRepository.find(user.getUserId(), listId)
                .orElseThrow(ListNotFoundException::new);
        TodoId todoId = todoRepository.nextIdentifier();
        todoListCommandModel.addDeferred(todoId, task);
        todoListRepository.save(todoListCommandModel);
    }

    public void delete(User user, ListId listId, TodoId todoId) {
        TodoListCommandModel todoListCommandModel = todoListRepository.find(user.getUserId(), listId)
                .orElseThrow(ListNotFoundException::new);
        todoListCommandModel.delete(todoId);
        todoListRepository.save(todoListCommandModel);
    }

    public void displace(User user, ListId listId, String task) {
        TodoListCommandModel todoListCommandModel = todoListRepository.find(user.getUserId(), listId)
                .orElseThrow(ListNotFoundException::new);
        TodoId todoId = todoRepository.nextIdentifier();
        todoListCommandModel.displace(todoId, task);
        todoListRepository.save(todoListCommandModel);
    }

    public void update(User user, ListId listId, TodoId todoId, String task) {
        TodoListCommandModel todoListCommandModel = todoListRepository.find(user.getUserId(), listId)
                .orElseThrow(ListNotFoundException::new);
        todoListCommandModel.update(todoId, task);
        todoListRepository.save(todoListCommandModel);
    }

    public void complete(User user, ListId listId, TodoId todoId) {
        TodoListCommandModel todoListCommandModel = todoListRepository.find(user.getUserId(), listId)
                .orElseThrow(ListNotFoundException::new);
        todoListCommandModel.complete(todoId);
        todoListRepository.save(todoListCommandModel);
    }

    public void move(User user, ListId listId, TodoId todoId, TodoId targetTodoId) {
        TodoListCommandModel todoListCommandModel = todoListRepository.find(user.getUserId(), listId)
                .orElseThrow(ListNotFoundException::new);
        todoListCommandModel.move(todoId, targetTodoId);
        todoListRepository.save(todoListCommandModel);
    }

    public void pull(User user, ListId listId) {
        TodoListCommandModel todoListCommandModel = todoListRepository.find(user.getUserId(), listId)
                .orElseThrow(ListNotFoundException::new);
        todoListCommandModel.pull();
        todoListRepository.save(todoListCommandModel);
    }

    @Override
    public void escalate(User user, ListId listId) {
        TodoListCommandModel todoListCommandModel = todoListRepository.find(user.getUserId(), listId)
                .orElseThrow(ListNotFoundException::new);
        todoListCommandModel.escalate();
        todoListRepository.save(todoListCommandModel);
    }
}
