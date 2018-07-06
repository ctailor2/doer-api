package com.doerapispring.domain;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class TodoService {
    private final ObjectRepository<CompletedList, String> completedListRepository;
    private final ListService listService;
    private final ObjectRepository<MasterList, String> masterListRepository;

    @Autowired
    TodoService(ListService listService,
                ObjectRepository<CompletedList, String> completedListRepository,
                ObjectRepository<MasterList, String> masterListRepository) {
        this.completedListRepository = completedListRepository;
        this.listService = listService;
        this.masterListRepository = masterListRepository;
    }

    public void create(User user, String task) throws OperationRefusedException {
        MasterList masterList = listService.get(user);
        try {
            // TODO: This should probably just return the localIdentifier, so the Todo has to be retrieved using a get to add it to the repo
            masterList.add(task);
            masterListRepository.save(masterList);
        } catch (ListSizeExceededException | AbnormalModelException e) {
            throw new OperationRefusedException();
        } catch (DuplicateTodoException e) {
            throw new OperationRefusedException(e.getMessage());
        }
    }

    public void createDeferred(User user, String task) throws OperationRefusedException {
        MasterList masterList = listService.get(user);
        try {
            // TODO: This should probably just return the localIdentifier, so the Todo has to be retrieved using a get to add it to the repo
            masterList.addDeferred(task);
            masterListRepository.save(masterList);
        } catch (AbnormalModelException e) {
            throw new OperationRefusedException();
        } catch (DuplicateTodoException e) {
            throw new OperationRefusedException(e.getMessage());
        }
    }

    public MasterList get(User user) throws OperationRefusedException {
        return listService.get(user);
    }

    public void delete(User user, String localIdentifier) throws OperationRefusedException {
        try {
            MasterList masterList = listService.get(user);
            masterList.delete(localIdentifier);
            masterListRepository.save(masterList);
        } catch (TodoNotFoundException | AbnormalModelException e) {
            throw new OperationRefusedException();
        }
    }

    public void displace(User user, String localIdentifier, String task) throws OperationRefusedException {
    }

    public void update(User user, String localIdentifier, String task) throws OperationRefusedException {
        try {
            MasterList masterList = listService.get(user);
            masterList.update(localIdentifier, task);
            masterListRepository.save(masterList);
        } catch (TodoNotFoundException | AbnormalModelException e) {
            throw new OperationRefusedException();
        } catch (DuplicateTodoException e) {
            throw new OperationRefusedException(e.getMessage());
        }
    }

    public void complete(User user, String localIdentifier) throws OperationRefusedException {
        try {
            MasterList masterList = listService.get(user);
            String task = masterList.complete(localIdentifier);
            masterListRepository.save(masterList);
            Optional<CompletedList> completedListOptional = completedListRepository.find(user.getIdentifier());
            if (completedListOptional.isPresent()) {
                CompletedList completedList = completedListOptional.get();
                completedList.add(task);
                try {
                    completedListRepository.save(completedList);
                } catch (AbnormalModelException e) {
                    throw new OperationRefusedException();
                }
            }
        } catch (TodoNotFoundException | AbnormalModelException e) {
            throw new OperationRefusedException();
        }
    }

    public CompletedList getCompleted(User user) throws OperationRefusedException {
        return completedListRepository.find(user.getIdentifier())
            .orElseThrow(OperationRefusedException::new);
    }

    public void move(User user, String localIdentifier, String targetLocalIdentifier) throws OperationRefusedException {
        try {
            MasterList masterList = listService.get(user);
            masterList.move(localIdentifier, targetLocalIdentifier);
            masterListRepository.save(masterList);
        } catch (TodoNotFoundException | AbnormalModelException e) {
            throw new OperationRefusedException();
        }
    }

    public void pull(User user) throws OperationRefusedException {
        try {
            MasterList masterList = listService.get(user);
            masterList.pull();
            masterListRepository.save(masterList);
        } catch (AbnormalModelException e) {
            throw new OperationRefusedException();
        }
    }

    public List<Todo> getDeferredTodos(User user) throws OperationRefusedException {
        try {
            MasterList masterList = listService.get(user);
            return masterList.getDeferredTodos();
        } catch (LockTimerNotExpiredException e) {
            throw new OperationRefusedException();
        }
    }
}
