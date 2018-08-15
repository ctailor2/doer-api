package com.doerapispring.domain;

import org.springframework.stereotype.Service;

@Service
public class ListService {
    private final ObjectRepository<MasterList, String> masterListRepository;
    private final ObjectRepository<CompletedList, String> completedListRepository;

    ListService(ObjectRepository<MasterList, String> masterListRepository,
                ObjectRepository<CompletedList, String> completedListRepository) {
        this.masterListRepository = masterListRepository;
        this.completedListRepository = completedListRepository;
    }

    public void unlock(User user) throws OperationRefusedException {
        MasterList masterList = get(user);
        try {
            masterList.unlock();
            masterListRepository.save(masterList);
        } catch (LockTimerNotExpiredException | AbnormalModelException e) {
            throw new OperationRefusedException();
        }
    }

    public MasterList get(User user) throws OperationRefusedException {
        return masterListRepository.find(user.getIdentifier())
            .orElseThrow(OperationRefusedException::new);
    }

    public CompletedList getCompleted(User user) throws OperationRefusedException {
        return completedListRepository.find(user.getIdentifier())
            .orElseThrow(OperationRefusedException::new);
    }
}
