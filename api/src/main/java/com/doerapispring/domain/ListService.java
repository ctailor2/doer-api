package com.doerapispring.domain;

import com.doerapispring.web.CompletedListDTO;
import com.doerapispring.web.CompletedTodoDTO;
import com.doerapispring.web.InvalidRequestException;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class ListService implements ListApplicationService {
    private final ObjectRepository<MasterList, String> masterListRepository;
    private final ObjectRepository<CompletedList, String> completedListRepository;
    private final ObjectRepository<ReadOnlyMasterList, String> readOnlyMasterListRepository;

    ListService(ObjectRepository<MasterList, String> masterListRepository,
                ObjectRepository<CompletedList, String> completedListRepository,
                ObjectRepository<ReadOnlyMasterList, String> readOnlyMasterListRepository) {
        this.masterListRepository = masterListRepository;
        this.completedListRepository = completedListRepository;
        this.readOnlyMasterListRepository = readOnlyMasterListRepository;
    }

    public void unlock(User user) throws InvalidRequestException {
        MasterList masterList = masterListRepository.find(user.getIdentifier())
            .orElseThrow(InvalidRequestException::new);
        try {
            masterList.unlock();
            masterListRepository.save(masterList);
        } catch (LockTimerNotExpiredException | AbnormalModelException e) {
            throw new InvalidRequestException();
        }
    }

    public ReadOnlyMasterList get(User user) throws InvalidRequestException {
        return readOnlyMasterListRepository.find(user.getIdentifier())
            .orElseThrow(InvalidRequestException::new);
    }

    public CompletedListDTO getCompleted(User user) throws InvalidRequestException {
        CompletedList completedList = completedListRepository.find(user.getIdentifier())
            .orElseThrow(InvalidRequestException::new);
        List<CompletedTodoDTO> completedTodoDTOs = completedList.getTodos().stream()
            .map(completedTodo -> new CompletedTodoDTO(completedTodo.getTask(), completedTodo.getCompletedAt()))
            .collect(toList());
        return new CompletedListDTO(completedTodoDTOs);
    }
}
