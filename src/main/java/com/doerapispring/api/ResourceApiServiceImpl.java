package com.doerapispring.api;

import com.doerapispring.authentication.AuthenticatedUser;
import com.doerapispring.domain.*;
import com.doerapispring.web.InvalidRequestException;
import com.doerapispring.web.ResourceApiService;
import com.doerapispring.web.TodoResourcesDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ResourceApiServiceImpl implements ResourceApiService {
    private final TodoService todoService;
    private final ListService listService;

    @Autowired
    ResourceApiServiceImpl(TodoService todoService, ListService listService) {
        this.todoService = todoService;
        this.listService = listService;
    }

    @Override
    public TodoResourcesDTO getTodoResources(AuthenticatedUser authenticatedUser) throws InvalidRequestException {
        try {
            TodoList nowList = todoService.getSubList(authenticatedUser.getUser(), ScheduledFor.now);
            ListManager listManager = listService.get(authenticatedUser.getUser());
            return new TodoResourcesDTO(nowList.isFull(), listManager.isLocked());
        } catch (OperationRefusedException e) {
            throw new InvalidRequestException();
        }
    }
}
