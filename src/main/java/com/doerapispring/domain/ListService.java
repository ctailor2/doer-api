package com.doerapispring.domain;

import org.springframework.stereotype.Service;

@Service
public class ListService {
    private final AggregateRootRepository<MasterList, ListUnlock> listUnlockRepository;
    private final ObjectRepository<MasterList, String> masterListRepository;

    ListService(ObjectRepository<MasterList, String> masterListRepository,
                AggregateRootRepository<MasterList, ListUnlock> listUnlockRepository) {
        this.masterListRepository = masterListRepository;
        this.listUnlockRepository = listUnlockRepository;
    }

    public void unlock(User user) throws OperationRefusedException {
        MasterList masterList = get(user);
        try {
            ListUnlock listUnlock = masterList.unlock();
            listUnlockRepository.add(masterList, listUnlock);
        } catch (LockTimerNotExpiredException | AbnormalModelException e) {
            throw new OperationRefusedException();
        }
    }

    public MasterList get(User user) throws OperationRefusedException {
        return masterListRepository.find(user.getIdentifier()).orElseThrow(OperationRefusedException::new);
    }
}
