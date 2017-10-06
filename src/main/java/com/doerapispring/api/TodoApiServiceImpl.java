package com.doerapispring.api;

import com.doerapispring.authentication.AuthenticatedUser;
import com.doerapispring.domain.*;
import com.doerapispring.web.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
class TodoApiServiceImpl implements TodoApiService {
    private final TodoService todoService;

    @Autowired
    TodoApiServiceImpl(TodoService todoService) {
        this.todoService = todoService;
    }

    @Override
    public TodoListDTO getTodos(AuthenticatedUser authenticatedUser) throws InvalidRequestException {
        try {
            MasterList masterList = todoService.get(authenticatedUser.getUser());
            List<TodoDTO> todoDTOs = masterList.getTodos()
                .stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
            return new TodoListDTO(todoDTOs, masterList.isFull());
        } catch (OperationRefusedException e) {
            throw new InvalidRequestException();
        }
    }

    @Override
    public TodoListDTO getDeferredTodos(AuthenticatedUser authenticatedUser) throws InvalidRequestException {
        try {
            List<TodoDTO> todoDTOs = todoService.getDeferredTodos(authenticatedUser.getUser())
                    .stream()
                    .map(this::mapToDTO)
                    .collect(Collectors.toList());
            // TODO: Returning a hardcoded value for this is weird - ideally shouldn't need this at all
            return new TodoListDTO(todoDTOs, false);
        } catch (OperationRefusedException e) {
            throw new InvalidRequestException();
        }
    }

    @Override
    public void create(AuthenticatedUser authenticatedUser, String task) throws InvalidRequestException {
        try {
            todoService.create(authenticatedUser.getUser(), task);
        } catch (OperationRefusedException e) {
            throw new InvalidRequestException();
        }
    }

    @Override
    public void createDeferred(AuthenticatedUser authenticatedUser, String task) throws InvalidRequestException {
        try {
            todoService.createDeferred(authenticatedUser.getUser(), task);
        } catch (OperationRefusedException e) {
            throw new InvalidRequestException();
        }
    }

    @Override
    public void delete(AuthenticatedUser authenticatedUser, String localId) throws InvalidRequestException {
        try {
            todoService.delete(authenticatedUser.getUser(), localId);
        } catch (OperationRefusedException e) {
            throw new InvalidRequestException();
        }
    }

    @Override
    public void displace(AuthenticatedUser authenticatedUser, String localId, String task) throws InvalidRequestException {
        try {
            todoService.displace(authenticatedUser.getUser(), localId, task);
        } catch (OperationRefusedException e) {
            throw new InvalidRequestException();
        }
    }

    @Override
    public void update(AuthenticatedUser authenticatedUser, String localId, String task) throws InvalidRequestException {
        try {
            todoService.update(authenticatedUser.getUser(), localId, task);
        } catch (OperationRefusedException e) {
            throw new InvalidRequestException();
        }
    }

    @Override
    public void complete(AuthenticatedUser authenticatedUser, String localId) throws InvalidRequestException {
        try {
            todoService.complete(authenticatedUser.getUser(), localId);
        } catch (OperationRefusedException e) {
            throw new InvalidRequestException();
        }
    }

    @Override
    public CompletedTodoListDTO getCompleted(AuthenticatedUser authenticatedUser) throws InvalidRequestException {
        try {
            CompletedList completedList = todoService.getCompleted(authenticatedUser.getUser());
            List<TodoDTO> todoDTOs = completedList.getTodos().stream().map(this::mapToDTO).collect(Collectors.toList());
            return new CompletedTodoListDTO(todoDTOs);
        } catch (OperationRefusedException e) {
            throw new InvalidRequestException();
        }
    }

    @Override
    public void move(AuthenticatedUser authenticatedUser, String localId, String targetLocalId) throws InvalidRequestException {
        try {
            todoService.move(authenticatedUser.getUser(), localId, targetLocalId);
        } catch (OperationRefusedException e) {
            throw new InvalidRequestException();
        }
    }

    @Override
    public void pull(AuthenticatedUser authenticatedUser) throws InvalidRequestException {
        try {
            todoService.pull(authenticatedUser.getUser());
        } catch (OperationRefusedException e) {
            throw new InvalidRequestException();
        }
    }

    // TODO: Maybe these behaviors should exist in a mapping layer of some sort
    private TodoDTO mapToDTO(Todo todo) {
        return new TodoDTO(todo.getLocalIdentifier(), todo.getTask());
    }

    // TODO: Maybe these behaviors should exist in a mapping layer of some sort
    private TodoDTO mapToDTO(CompletedTodo completedTodo) {
        return new TodoDTO(completedTodo.getTask(), completedTodo.getCompletedAt());
    }
}
