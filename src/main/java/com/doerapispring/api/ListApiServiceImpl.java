package com.doerapispring.api;

import com.doerapispring.authentication.AuthenticatedUser;
import com.doerapispring.domain.ListService;
import com.doerapispring.domain.MasterList;
import com.doerapispring.domain.OperationRefusedException;
import com.doerapispring.web.InvalidRequestException;
import com.doerapispring.web.ListApiService;
import com.doerapispring.web.MasterListDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

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
                masterList.isFull(),
                masterList.isLocked());
        } catch (OperationRefusedException e) {
            throw new InvalidRequestException();
        }
    }
}
