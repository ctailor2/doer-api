package com.doerapispring.domain;

import org.springframework.stereotype.Service;

@Service
public class ListService {
    private final AggregateRootRepository<ListManager, ListUnlock, String> listUnlockRepository;
    private final AggregateRootRepository<MasterList, Todo, String> masterListRepository;

    ListService(AggregateRootRepository<ListManager, ListUnlock, String> listUnlockRepository,
                AggregateRootRepository<MasterList, Todo, String> masterListRepository) {
        this.listUnlockRepository = listUnlockRepository;
        this.masterListRepository = masterListRepository;
    }

    public ListManager getListManager(User user) throws OperationRefusedException {
        return listUnlockRepository.find(user.getIdentifier())
                .orElseThrow(OperationRefusedException::new);
    }

    public void unlock(User user) throws OperationRefusedException {
        ListManager listManager = getListManager(user);
        try {
            ListUnlock listUnlock = listManager.unlock();
            listUnlockRepository.add(listManager, listUnlock);
        } catch (LockTimerNotExpiredException | AbnormalModelException e) {
            throw new OperationRefusedException();
        }
    }

    public MasterList get(User user) throws OperationRefusedException {
        return masterListRepository.find(user.getIdentifier()).orElseThrow(OperationRefusedException::new);
    }
}
