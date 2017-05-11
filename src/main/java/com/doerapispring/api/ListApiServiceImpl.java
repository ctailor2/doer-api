package com.doerapispring.api;

import com.doerapispring.authentication.AuthenticatedUser;
import com.doerapispring.domain.ListService;
import com.doerapispring.domain.OperationRefusedException;
import com.doerapispring.web.InvalidRequestException;
import com.doerapispring.web.ListApiService;
import com.doerapispring.web.ListDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

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
    public List<ListDTO> getAll(AuthenticatedUser authenticatedUser) throws InvalidRequestException {
        try {
            return listService.getAll().stream()
                    .map(basicTodoList -> new ListDTO(basicTodoList.getName()))
                    .collect(Collectors.toList());
        } catch (OperationRefusedException e) {
            throw new InvalidRequestException();
        }
    }
}
