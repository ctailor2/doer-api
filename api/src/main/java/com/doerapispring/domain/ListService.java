package com.doerapispring.domain;

import com.doerapispring.web.*;
import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.stream.Collectors.toList;

@Service
public class ListService implements ListApplicationService {
    private final ObjectRepository<MasterList, String> masterListRepository;
    private final ObjectRepository<CompletedList, String> completedListRepository;

    ListService(ObjectRepository<MasterList, String> masterListRepository,
                ObjectRepository<CompletedList, String> completedListRepository) {
        this.masterListRepository = masterListRepository;
        this.completedListRepository = completedListRepository;
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

    public MasterListDTO get(User user) throws InvalidRequestException {
//        TODO: This should probably just return a MasterList read model that is different from the write model
//        which already exposes the query methods like isFull, isAbleToBeUnlocked, isAbleToBeReplenished
//        along with it's state
        MasterList masterList = masterListRepository.find(user.getIdentifier())
            .orElseThrow(InvalidRequestException::new);
        return new MasterListDTO(
            masterList.getName(),
            masterList.getDeferredName(),
            masterList.getTodos().stream().map(todo -> new TodoDTO(todo.getTodoId().getIdentifier(), todo.getTask())).collect(toList()),
            masterList.getDeferredTodos().stream().map(todo -> new TodoDTO(todo.getTodoId().getIdentifier(), todo.getTask())).collect(toList()),
            masterList.unlockDuration(),
            masterList.isFull(),
            masterList.isAbleToBeUnlocked(),
            masterList.isAbleToBeReplenished());
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
