package com.doerapispring.domain;

import org.springframework.stereotype.Service;

@Service
public class ListService {
    private final AggregateRootRepository<ListManager, ListUnlock, String> listViewRepository;

    ListService(AggregateRootRepository<ListManager, ListUnlock, String> listViewRepository) {
        this.listViewRepository = listViewRepository;
    }

    public ListManager get(User user) throws OperationRefusedException {
        return listViewRepository.find(user.getIdentifier())
                .orElseThrow(OperationRefusedException::new);
    }

    public void unlock(User user) throws OperationRefusedException {
        ListManager listViewManager = get(user);
        try {
            ListUnlock listUnlock = listViewManager.unlock();
            listViewRepository.add(listViewManager, listUnlock);
        } catch (LockTimerNotExpiredException | AbnormalModelException e) {
            throw new OperationRefusedException();
        }
    }
}
