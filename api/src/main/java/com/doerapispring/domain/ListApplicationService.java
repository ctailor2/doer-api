package com.doerapispring.domain;

import com.doerapispring.web.CompletedListDTO;
import com.doerapispring.web.InvalidRequestException;

public interface ListApplicationService {
    void unlock(User user) throws InvalidRequestException;

    ReadOnlyMasterList get(User user) throws InvalidRequestException;

    CompletedListDTO getCompleted(User user) throws InvalidRequestException;
}
