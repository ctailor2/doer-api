package com.doerapispring.api;

import com.doerapispring.authentication.AuthenticatedUser;
import com.doerapispring.domain.*;
import com.doerapispring.web.InvalidRequestException;
import com.doerapispring.web.TodoApiService;
import com.doerapispring.web.TodoDTO;
import com.doerapispring.web.TodoListDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class TodoApiServiceImpl implements TodoApiService {
    private final TodoService todoService;

    @Autowired
    public TodoApiServiceImpl(TodoService todoService) {
        this.todoService = todoService;
    }

    @Override
    public TodoListDTO get(AuthenticatedUser authenticatedUser) throws InvalidRequestException {
        MasterList masterList;
        try {
            masterList = todoService.get(authenticatedUser.getUser());
        } catch (OperationRefusedException e) {
            throw new InvalidRequestException();
        }
        List<TodoDTO> todoDTOs = masterList.getTodos().stream().map(this::mapToDTO).collect(Collectors.toList());
        return new TodoListDTO(todoDTOs, !masterList.isImmediateListFull());
    }

    @Override
    public void create(AuthenticatedUser authenticatedUser, String task, String scheduling) throws InvalidRequestException {
        ScheduledFor scheduledFor = getScheduledFor(scheduling);
        try {
            todoService.create(authenticatedUser.getUser(), task, scheduledFor);
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
            todoService.displace(authenticatedUser.getUser(), localId, "someTask");
        } catch (OperationRefusedException e) {
            throw new InvalidRequestException();
        }
    }

    // TODO: Maybe these behaviors should exist in a mapping layer of some sort
    private TodoDTO mapToDTO(Todo todo) {
        return new TodoDTO(todo.getLocalIdentifier(), todo.getTask(), todo.getScheduling().toString());
    }

    private ScheduledFor getScheduledFor(String scheduling) throws InvalidRequestException {
        try {
            return Enum.valueOf(ScheduledFor.class, scheduling);
        } catch (IllegalArgumentException e) {
            throw new InvalidRequestException();
        }
    }
}
