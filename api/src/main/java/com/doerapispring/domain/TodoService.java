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

    public void create(User user, ListId listId, String task) throws InvalidCommandException {
        TodoListCommandModel todoListCommandModel = todoListRepository.find(user.getUserId(), listId)
            .orElseThrow(InvalidCommandException::new);
        TodoId todoId = todoRepository.nextIdentifier();
        try {
            todoListCommandModel.add(todoId, task);
            todoListRepository.save(todoListCommandModel);
        } catch (ListSizeExceededException e) {
            throw new InvalidCommandException();
        } catch (DuplicateTodoException e) {
            throw new InvalidCommandException(e.getMessage());
        }
    }

    public void createDeferred(User user, ListId listId, String task) throws InvalidCommandException {
        TodoListCommandModel todoListCommandModel = todoListRepository.find(user.getUserId(), listId)
            .orElseThrow(InvalidCommandException::new);
        TodoId todoId = todoRepository.nextIdentifier();
        try {
            todoListCommandModel.addDeferred(todoId, task);
            todoListRepository.save(todoListCommandModel);
        } catch (DuplicateTodoException e) {
            throw new InvalidCommandException(e.getMessage());
        }
    }

    public void delete(User user, ListId listId, TodoId todoId) throws InvalidCommandException {
        TodoListCommandModel todoListCommandModel = todoListRepository.find(user.getUserId(), listId)
            .orElseThrow(InvalidCommandException::new);
        try {
            todoListCommandModel.delete(todoId);
            todoListRepository.save(todoListCommandModel);
        } catch (TodoNotFoundException e) {
            throw new InvalidCommandException();
        }
    }

    public void displace(User user, ListId listId, String task) throws InvalidCommandException {
        TodoListCommandModel todoListCommandModel = todoListRepository.find(user.getUserId(), listId)
            .orElseThrow(InvalidCommandException::new);
        TodoId todoId = todoRepository.nextIdentifier();
        try {
            todoListCommandModel.displace(todoId, task);
            todoListRepository.save(todoListCommandModel);
        } catch (ListNotFullException e) {
            throw new InvalidCommandException();
        } catch (DuplicateTodoException e) {
            throw new InvalidCommandException(e.getMessage());
        }
    }

    public void update(User user, ListId listId, TodoId todoId, String task) throws InvalidCommandException {
        TodoListCommandModel todoListCommandModel = todoListRepository.find(user.getUserId(), listId)
            .orElseThrow(InvalidCommandException::new);
        try {
            todoListCommandModel.update(todoId, task);
            todoListRepository.save(todoListCommandModel);
        } catch (TodoNotFoundException e) {
            throw new InvalidCommandException();
        } catch (DuplicateTodoException e) {
            throw new InvalidCommandException(e.getMessage());
        }
    }

    public void complete(User user, ListId listId, TodoId todoId) throws InvalidCommandException {
        TodoListCommandModel todoListCommandModel = todoListRepository.find(user.getUserId(), listId)
            .orElseThrow(InvalidCommandException::new);
        try {
            todoListCommandModel.complete(todoId);
            todoListRepository.save(todoListCommandModel);
        } catch (TodoNotFoundException e) {
            throw new InvalidCommandException();
        }
    }

    public void move(User user, ListId listId, TodoId todoId, TodoId targetTodoId) throws InvalidCommandException {
        TodoListCommandModel todoListCommandModel = todoListRepository.find(user.getUserId(), listId)
            .orElseThrow(InvalidCommandException::new);
        try {
            todoListCommandModel.move(todoId, targetTodoId);
            todoListRepository.save(todoListCommandModel);
        } catch (TodoNotFoundException e) {
            throw new InvalidCommandException();
        }
    }

    public void pull(User user, ListId listId) throws InvalidCommandException {
        TodoListCommandModel todoListCommandModel = todoListRepository.find(user.getUserId(), listId)
            .orElseThrow(InvalidCommandException::new);
        todoListCommandModel.pull();
        todoListRepository.save(todoListCommandModel);
    }

    @Override
    public void escalate(User user, ListId listId) throws InvalidCommandException {
        TodoListCommandModel todoListCommandModel = todoListRepository.find(user.getUserId(), listId)
            .orElseThrow(InvalidCommandException::new);
        try {
            todoListCommandModel.escalate();
            todoListRepository.save(todoListCommandModel);
        } catch (EscalateNotAllowException e) {
            throw new InvalidCommandException();
        }
    }
}
