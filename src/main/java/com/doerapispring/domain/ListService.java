package com.doerapispring.domain;

import org.springframework.stereotype.Service;

@Service
public class ListService {
    private final AggregateRootRepository<ListManager, ListUnlock, String> listUnlockRepository;

    ListService(AggregateRootRepository<ListManager, ListUnlock, String> listUnlockRepository) {
        this.listUnlockRepository = listUnlockRepository;
    }

    public ListManager get(User user) throws OperationRefusedException {
        return listUnlockRepository.find(user.getIdentifier())
                .orElseThrow(OperationRefusedException::new);
    }

    public void unlock(User user) throws OperationRefusedException {
        ListManager listManager = get(user);
        try {
            ListUnlock listUnlock = listManager.unlock();
            listUnlockRepository.add(listManager, listUnlock);
        } catch (LockTimerNotExpiredException | AbnormalModelException e) {
            throw new OperationRefusedException();
        }
    }
}
