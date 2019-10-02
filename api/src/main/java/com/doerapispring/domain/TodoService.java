package com.doerapispring.domain;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TodoService implements TodoApplicationService {
    private final OwnedObjectRepository<TodoList, UserId, ListId> todoListRepository;
    private final IdentityGeneratingRepository<TodoId> todoRepository;
    private final DomainEventPublisher domainEventPublisher;

    TodoService(OwnedObjectRepository<TodoList, UserId, ListId> todoListRepository,
                IdentityGeneratingRepository<TodoId> todoRepository,
                DomainEventPublisher domainEventPublisher) {
        this.todoListRepository = todoListRepository;
        this.todoRepository = todoRepository;
        this.domainEventPublisher = domainEventPublisher;
    }

    public void create(User user, ListId listId, String task) throws InvalidCommandException {
        TodoList todoList = todoListRepository.find(user.getUserId(), listId)
            .orElseThrow(InvalidCommandException::new);
        TodoId todoId = todoRepository.nextIdentifier();
        try {
            todoList.add(todoId, task);
            todoListRepository.save(todoList);
        } catch (ListSizeExceededException e) {
            throw new InvalidCommandException();
        } catch (DuplicateTodoException e) {
            throw new InvalidCommandException(e.getMessage());
        }
    }

    public void createDeferred(User user, ListId listId, String task) throws InvalidCommandException {
        TodoList todoList = todoListRepository.find(user.getUserId(), listId)
            .orElseThrow(InvalidCommandException::new);
        TodoId todoId = todoRepository.nextIdentifier();
        try {
            todoList.addDeferred(todoId, task);
            todoListRepository.save(todoList);
        } catch (DuplicateTodoException e) {
            throw new InvalidCommandException(e.getMessage());
        }
    }

    public void delete(User user, ListId listId, TodoId todoId) throws InvalidCommandException {
        TodoList todoList = todoListRepository.find(user.getUserId(), listId)
            .orElseThrow(InvalidCommandException::new);
        try {
            todoList.delete(todoId);
            todoListRepository.save(todoList);
        } catch (TodoNotFoundException e) {
            throw new InvalidCommandException();
        }
    }

    public void displace(User user, ListId listId, String task) throws InvalidCommandException {
        TodoList todoList = todoListRepository.find(user.getUserId(), listId)
            .orElseThrow(InvalidCommandException::new);
        TodoId todoId = todoRepository.nextIdentifier();
        try {
            todoList.displace(todoId, task);
            todoListRepository.save(todoList);
        } catch (ListNotFullException e) {
            throw new InvalidCommandException();
        } catch (DuplicateTodoException e) {
            throw new InvalidCommandException(e.getMessage());
        }
    }

    public void update(User user, ListId listId, TodoId todoId, String task) throws InvalidCommandException {
        TodoList todoList = todoListRepository.find(user.getUserId(), listId)
            .orElseThrow(InvalidCommandException::new);
        try {
            todoList.update(todoId, task);
            todoListRepository.save(todoList);
        } catch (TodoNotFoundException e) {
            throw new InvalidCommandException();
        } catch (DuplicateTodoException e) {
            throw new InvalidCommandException(e.getMessage());
        }
    }

    public void complete(User user, ListId listId, TodoId todoId) throws InvalidCommandException {
        TodoList todoList = todoListRepository.find(user.getUserId(), listId)
            .orElseThrow(InvalidCommandException::new);
        try {
            todoList.complete(todoId);
            todoListRepository.save(todoList);
            domainEventPublisher.publish(todoList);
        } catch (TodoNotFoundException e) {
            throw new InvalidCommandException();
        }
    }

    public void move(User user, ListId listId, TodoId todoId, TodoId targetTodoId) throws InvalidCommandException {
        TodoList todoList = todoListRepository.find(user.getUserId(), listId)
            .orElseThrow(InvalidCommandException::new);
        try {
            todoList.move(todoId, targetTodoId);
            todoListRepository.save(todoList);
        } catch (TodoNotFoundException e) {
            throw new InvalidCommandException();
        }
    }

    public void pull(User user, ListId listId) throws InvalidCommandException {
        TodoList todoList = todoListRepository.find(user.getUserId(), listId)
            .orElseThrow(InvalidCommandException::new);
        todoList.pull();
        todoListRepository.save(todoList);
    }

    @Override
    public void escalate(User user, ListId listId) throws InvalidCommandException {
        TodoList todoList = todoListRepository.find(user.getUserId(), listId)
            .orElseThrow(InvalidCommandException::new);
        try {
            todoList.escalate();
            todoListRepository.save(todoList);
        } catch (EscalateNotAllowException e) {
            throw new InvalidCommandException();
        }
    }
}
