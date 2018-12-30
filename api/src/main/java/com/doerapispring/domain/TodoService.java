package com.doerapispring.domain;

import com.doerapispring.web.InvalidRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TodoService implements TodoApplicationService {
    private final OwnedObjectRepository<TodoList, UserId, ListId> todoListRepository;
    private final IdentityGeneratingRepository<TodoId> todoRepository;
    private final OwnedObjectRepository<CompletedTodo, UserId, CompletedTodoId> completedTodoRepository;

    TodoService(OwnedObjectRepository<TodoList, UserId, ListId> todoListRepository,
                IdentityGeneratingRepository<TodoId> todoRepository,
                OwnedObjectRepository<CompletedTodo, UserId, CompletedTodoId> completedTodoRepository) {
        this.todoListRepository = todoListRepository;
        this.todoRepository = todoRepository;
        this.completedTodoRepository = completedTodoRepository;
    }

    public void create(User user, ListId listId, String task) throws InvalidRequestException {
        TodoList todoList = todoListRepository.find(user.getUserId(), listId)
            .orElseThrow(InvalidRequestException::new);
        TodoId todoId = todoRepository.nextIdentifier();
        try {
            todoList.add(todoId, task);
            todoListRepository.save(todoList);
        } catch (ListSizeExceededException | AbnormalModelException e) {
            throw new InvalidRequestException();
        } catch (DuplicateTodoException e) {
            throw new InvalidRequestException(e.getMessage());
        }
    }

    public void createDeferred(User user, ListId listId, String task) throws InvalidRequestException {
        TodoList todoList = todoListRepository.find(user.getUserId(), listId)
            .orElseThrow(InvalidRequestException::new);
        TodoId todoId = todoRepository.nextIdentifier();
        try {
            todoList.addDeferred(todoId, task);
            todoListRepository.save(todoList);
        } catch (AbnormalModelException e) {
            throw new InvalidRequestException();
        } catch (DuplicateTodoException e) {
            throw new InvalidRequestException(e.getMessage());
        }
    }

    public void delete(User user, ListId listId, TodoId todoId) throws InvalidRequestException {
        TodoList todoList = todoListRepository.find(user.getUserId(), listId)
            .orElseThrow(InvalidRequestException::new);
        try {
            todoList.delete(todoId);
            todoListRepository.save(todoList);
        } catch (TodoNotFoundException | AbnormalModelException e) {
            throw new InvalidRequestException();
        }
    }

    public void displace(User user, ListId listId, String task) throws InvalidRequestException {
        TodoList todoList = todoListRepository.find(user.getUserId(), listId)
            .orElseThrow(InvalidRequestException::new);
        TodoId todoId = todoRepository.nextIdentifier();
        try {
            todoList.displace(todoId, task);
            todoListRepository.save(todoList);
        } catch (AbnormalModelException | DuplicateTodoException | ListNotFullException e) {
            throw new InvalidRequestException(e.getMessage());
        }
    }

    public void update(User user, ListId listId, TodoId todoId, String task) throws InvalidRequestException {
        TodoList todoList = todoListRepository.find(user.getUserId(), listId)
            .orElseThrow(InvalidRequestException::new);
        try {
            todoList.update(todoId, task);
            todoListRepository.save(todoList);
        } catch (TodoNotFoundException | AbnormalModelException e) {
            throw new InvalidRequestException();
        } catch (DuplicateTodoException e) {
            throw new InvalidRequestException(e.getMessage());
        }
    }

    public void complete(User user, ListId listId, TodoId todoId) throws InvalidRequestException {
        TodoList todoList = todoListRepository.find(user.getUserId(), listId)
            .orElseThrow(InvalidRequestException::new);
        try {
            CompletedTodo completedTodo = todoList.complete(todoId);
            todoListRepository.save(todoList);
            completedTodoRepository.save(completedTodo);
        } catch (TodoNotFoundException | AbnormalModelException e) {
            throw new InvalidRequestException();
        }
    }

    public void move(User user, ListId listId, TodoId todoId, TodoId targetTodoId) throws InvalidRequestException {
        TodoList todoList = todoListRepository.find(user.getUserId(), listId)
            .orElseThrow(InvalidRequestException::new);
        try {
            todoList.move(todoId, targetTodoId);
            todoListRepository.save(todoList);
        } catch (TodoNotFoundException | AbnormalModelException e) {
            throw new InvalidRequestException();
        }
    }

    public void pull(User user, ListId listId) throws InvalidRequestException {
        TodoList todoList = todoListRepository.find(user.getUserId(), listId)
            .orElseThrow(InvalidRequestException::new);
        try {
            todoList.pull();
            todoListRepository.save(todoList);
        } catch (AbnormalModelException e) {
            throw new InvalidRequestException();
        }
    }

    @Override
    public void escalate(User user, ListId listId) throws InvalidRequestException {
        TodoList todoList = todoListRepository.find(user.getUserId(), listId)
            .orElseThrow(InvalidRequestException::new);
        try {
            todoList.escalate();
            todoListRepository.save(todoList);
        } catch (AbnormalModelException | EscalateNotAllowException e) {
            throw new InvalidRequestException();
        }
    }
}
