package com.doerapispring.api;

import com.doerapispring.authentication.AuthenticatedUser;
import com.doerapispring.domain.ListService;
import com.doerapispring.domain.MasterList;
import com.doerapispring.domain.OperationRefusedException;
import com.doerapispring.web.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import static java.util.stream.Collectors.toList;

@Service
class ListApiServiceImpl implements ListApiService {
    private final ListService listService;

    @Autowired
    ListApiServiceImpl(ListService listService) {
        this.listService = listService;
    }

    @Override
    public void unlock(AuthenticatedUser authenticatedUser) throws InvalidRequestException {
        try {
            listService.unlock(authenticatedUser.getUser());
        } catch (OperationRefusedException e) {
            throw new InvalidRequestException();
        }
    }

    @Override
    public MasterListDTO get(AuthenticatedUser authenticatedUser) throws InvalidRequestException {
        try {
            MasterList masterList = listService.get(authenticatedUser.getUser());
            return new MasterListDTO(
                masterList.getName(),
                masterList.getDeferredName(),
                masterList.getTodos().stream().map(todo -> new TodoDTO(todo.getLocalIdentifier(), todo.getTask())).collect(toList()),
                masterList.getDeferredTodos().stream().map(todo -> new TodoDTO(todo.getLocalIdentifier(), todo.getTask())).collect(toList()),
                masterList.unlockDuration(),
                masterList.isFull(),
                masterList.isAbleToBeUnlocked(),
                masterList.isAbleToBeReplenished());
        } catch (OperationRefusedException e) {
            throw new InvalidRequestException();
        }
    }

    @Override
    public CompletedListDTO getCompleted(AuthenticatedUser authenticatedUser) throws InvalidRequestException {
        return new CompletedListDTO();
    }
}
