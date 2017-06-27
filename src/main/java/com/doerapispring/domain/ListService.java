package com.doerapispring.domain;

import org.springframework.stereotype.Service;

import java.util.List;

import static java.util.Arrays.asList;

@Service
public class ListService {
    private static final String PRIMARY_LIST_NAME = "now";
    private static final String SECONDARY_LIST_NAME = "later";

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

    public List<BasicTodoList> getAll() {
        return asList(
                new BasicTodoList(PRIMARY_LIST_NAME),
                new BasicTodoList(SECONDARY_LIST_NAME));
    }

    public TodoList get(User user) throws OperationRefusedException {
        MasterList masterList = masterListRepository.find(user.getIdentifier()).orElseThrow(OperationRefusedException::new);
        return masterList.getImmediateList();
    }
}
