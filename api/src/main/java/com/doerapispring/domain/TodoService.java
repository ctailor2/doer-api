package com.doerapispring.domain;

import com.doerapispring.web.InvalidRequestException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class TodoService implements TodoApplicationService {
    private final IdentityGeneratingObjectRepository<CompletedList, String> completedListRepository;
    private final OwnedObjectRepository<TodoList, UserId, ListId> todoListRepository;
    private final IdentityGeneratingRepository<TodoId> todoRepository;

    TodoService(IdentityGeneratingObjectRepository<CompletedList, String> completedListRepository,
                OwnedObjectRepository<TodoList, UserId, ListId> todoListRepository,
                IdentityGeneratingRepository<TodoId> todoRepository) {
        this.completedListRepository = completedListRepository;
        this.todoListRepository = todoListRepository;
        this.todoRepository = todoRepository;
    }

    public void create(User user, String task) throws InvalidRequestException {
        TodoList todoList = todoListRepository.findOne(user.getUserId())
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

    public void createDeferred(User user, String task) throws InvalidRequestException {
        TodoList todoList = todoListRepository.findOne(user.getUserId())
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

    public void delete(User user, TodoId todoId) throws InvalidRequestException {
        TodoList todoList = todoListRepository.findOne(user.getUserId())
            .orElseThrow(InvalidRequestException::new);
        try {
            todoList.delete(todoId);
            todoListRepository.save(todoList);
        } catch (TodoNotFoundException | AbnormalModelException e) {
            throw new InvalidRequestException();
        }
    }

    public void displace(User user, String task) throws InvalidRequestException {
        TodoList todoList = todoListRepository.findOne(user.getUserId())
            .orElseThrow(InvalidRequestException::new);
        TodoId todoId = todoRepository.nextIdentifier();
        try {
            todoList.displace(todoId, task);
            todoListRepository.save(todoList);
        } catch (AbnormalModelException | DuplicateTodoException | ListNotFullException e) {
            throw new InvalidRequestException(e.getMessage());
        }
    }

    public void update(User user, TodoId todoId, String task) throws InvalidRequestException {
        TodoList todoList = todoListRepository.findOne(user.getUserId())
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

    public void complete(User user, TodoId todoId) throws InvalidRequestException {
        TodoList todoList = todoListRepository.findOne(user.getUserId())
            .orElseThrow(InvalidRequestException::new);
        try {
            String task = todoList.complete(todoId);
            todoListRepository.save(todoList);
            Optional<CompletedList> completedListOptional = completedListRepository.find(user.getIdentifier());
            if (completedListOptional.isPresent()) {
                UniqueIdentifier<String> completedTodoIdentifier = completedListRepository.nextIdentifier();
                CompletedList completedList = completedListOptional.get();
                completedList.add(new CompletedTodoId(completedTodoIdentifier.get()), task);
                try {
                    completedListRepository.save(completedList);
                } catch (AbnormalModelException e) {
                    throw new InvalidRequestException();
                }
            }
        } catch (TodoNotFoundException | AbnormalModelException e) {
            throw new InvalidRequestException();
        }
    }

    public void move(User user, TodoId todoId, TodoId targetTodoId) throws InvalidRequestException {
        TodoList todoList = todoListRepository.findOne(user.getUserId())
            .orElseThrow(InvalidRequestException::new);
        try {
            todoList.move(todoId, targetTodoId);
            todoListRepository.save(todoList);
        } catch (TodoNotFoundException | AbnormalModelException e) {
            throw new InvalidRequestException();
        }
    }

    public void pull(User user) throws InvalidRequestException {
        TodoList todoList = todoListRepository.findOne(user.getUserId())
            .orElseThrow(InvalidRequestException::new);
        try {
            todoList.pull();
            todoListRepository.save(todoList);
        } catch (AbnormalModelException e) {
            throw new InvalidRequestException();
        }
    }
}
