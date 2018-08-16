package com.doerapispring.domain;

import com.doerapispring.web.CompletedListDTO;
import com.doerapispring.web.InvalidRequestException;
import com.doerapispring.web.MasterListDTO;

public interface ListApplicationService {
    void unlock(User user) throws InvalidRequestException;

    MasterListDTO get(User user) throws InvalidRequestException;

    CompletedListDTO getCompleted(User user) throws InvalidRequestException;
}
