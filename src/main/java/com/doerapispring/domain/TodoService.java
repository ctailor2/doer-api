package com.doerapispring.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class TodoService {
    private final AggregateRootRepository<MasterList, Todo, String> masterListRepository;

    @Autowired
    TodoService(AggregateRootRepository<MasterList, Todo, String> masterListRepository) {
        this.masterListRepository = masterListRepository;
    }

    public void create(User user, String task, ScheduledFor scheduling) throws OperationRefusedException {
        MasterList masterList = get(user);
        try {
            // TODO: This should probably just return the localIdentifier, so the Todo has to be retrieved using a get to add it to the repo
            Todo todo = masterList.add(task, scheduling);
            masterListRepository.add(masterList, todo);
        } catch (ListSizeExceededException | DuplicateTodoException | AbnormalModelException e) {
            throw new OperationRefusedException();
        }
    }

    public MasterList get(User user) throws OperationRefusedException {
        return masterListRepository.find(user.getIdentifier())
                .orElseThrow(OperationRefusedException::new);
    }

    public void delete(User user, Integer localIdentifier) throws OperationRefusedException {
        try {
            MasterList masterList = get(user);
            Todo todo = masterList.delete(localIdentifier);
            masterListRepository.remove(masterList, todo);
        } catch (TodoNotFoundException | AbnormalModelException e) {
            throw new OperationRefusedException();
        }
    }
}
