package com.doerapispring.domain;

import org.springframework.stereotype.Service;

@Service
public class ListViewService {
    private final AggregateRootRepository<ListViewManager, ListView, String> listViewRepository;

    ListViewService(AggregateRootRepository<ListViewManager, ListView, String> listViewRepository) {
        this.listViewRepository = listViewRepository;
    }

    public ListViewManager get(User user) throws OperationRefusedException {
        return listViewRepository.find(user.getIdentifier())
                .orElseThrow(OperationRefusedException::new);
    }

    public void create(User user) throws OperationRefusedException {
        ListViewManager listViewManager = get(user);
        try {
            ListView listView = listViewManager.recordView();
            listViewRepository.add(listViewManager, listView);
        } catch (LockTimerNotExpiredException | AbnormalModelException e) {
            throw new OperationRefusedException();
        }
    }
}
