package com.doerapispring.domain;

import com.doerapispring.web.InvalidRequestException;
import org.springframework.stereotype.Service;

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

    public ReadOnlyMasterList get(User user) throws InvalidRequestException {
        return masterListRepository.find(user.getIdentifier())
            .map(MasterList::read)
            .orElseThrow(InvalidRequestException::new);
    }

    public ReadOnlyCompletedList getCompleted(User user) throws InvalidRequestException {
        return completedListRepository.find(user.getIdentifier())
            .map(CompletedList::read)
            .orElseThrow(InvalidRequestException::new);
    }
}
