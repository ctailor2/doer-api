package com.doerapispring.domain;

import com.doerapispring.storage.IdentityGeneratingObjectRepository;
import com.doerapispring.web.InvalidRequestException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@Transactional
public class TodoService implements TodoApplicationService {
    private final IdentityGeneratingObjectRepository<CompletedList, String> completedListRepository;
    private final IdentityGeneratingObjectRepository<MasterList, String> masterListRepository;

    @Autowired
    TodoService(IdentityGeneratingObjectRepository<CompletedList, String> completedListRepository,
                IdentityGeneratingObjectRepository<MasterList, String> masterListRepository) {
        this.completedListRepository = completedListRepository;
        this.masterListRepository = masterListRepository;
    }

    public void create(User user, String task) throws InvalidRequestException {
        MasterList masterList = masterListRepository.find(user.getIdentifier())
            .orElseThrow(InvalidRequestException::new);
        UniqueIdentifier<String> todoIdentifier = masterListRepository.nextIdentifier();
        try {
            masterList.add(new TodoId(todoIdentifier.get()), task);
            masterListRepository.save(masterList);
        } catch (ListSizeExceededException | AbnormalModelException e) {
            throw new InvalidRequestException();
        } catch (DuplicateTodoException e) {
            throw new InvalidRequestException(e.getMessage());
        }
    }

    public void createDeferred(User user, String task) throws InvalidRequestException {
        MasterList masterList = masterListRepository.find(user.getIdentifier())
            .orElseThrow(InvalidRequestException::new);
        UniqueIdentifier<String> todoIdentifier = masterListRepository.nextIdentifier();
        try {
            masterList.addDeferred(new TodoId(todoIdentifier.get()), task);
            masterListRepository.save(masterList);
        } catch (AbnormalModelException e) {
            throw new InvalidRequestException();
        } catch (DuplicateTodoException e) {
            throw new InvalidRequestException(e.getMessage());
        }
    }

    public void delete(User user, String localIdentifier) throws InvalidRequestException {
        MasterList masterList = masterListRepository.find(user.getIdentifier())
            .orElseThrow(InvalidRequestException::new);
        try {
            masterList.delete(localIdentifier);
            masterListRepository.save(masterList);
        } catch (TodoNotFoundException | AbnormalModelException e) {
            throw new InvalidRequestException();
        }
    }

    public void displace(User user, String task) throws InvalidRequestException {
        MasterList masterList = masterListRepository.find(user.getIdentifier())
            .orElseThrow(InvalidRequestException::new);
        UniqueIdentifier<String> todoIdentifier = masterListRepository.nextIdentifier();
        try {
            masterList.displace(new TodoId(todoIdentifier.get()), task);
            masterListRepository.save(masterList);
        } catch (AbnormalModelException | DuplicateTodoException | ListNotFullException e) {
            throw new InvalidRequestException(e.getMessage());
        }
    }

    public void update(User user, String localIdentifier, String task) throws InvalidRequestException {
        MasterList masterList = masterListRepository.find(user.getIdentifier())
            .orElseThrow(InvalidRequestException::new);
        try {
            masterList.update(localIdentifier, task);
            masterListRepository.save(masterList);
        } catch (TodoNotFoundException | AbnormalModelException e) {
            throw new InvalidRequestException();
        } catch (DuplicateTodoException e) {
            throw new InvalidRequestException(e.getMessage());
        }
    }

    public void complete(User user, String localIdentifier) throws InvalidRequestException {
        MasterList masterList = masterListRepository.find(user.getIdentifier())
            .orElseThrow(InvalidRequestException::new);
        try {
            String task = masterList.complete(localIdentifier);
            masterListRepository.save(masterList);
            Optional<CompletedList> completedListOptional = completedListRepository.find(user.getIdentifier());
            if (completedListOptional.isPresent()) {
                UniqueIdentifier<String> completedTodoIdentifier = completedListRepository.nextIdentifier();
                CompletedList completedList = completedListOptional.get();
                completedList.add(new CompletedTodoId(completedTodoIdentifier.get()), task);
                try {
                    completedListRepository.save(completedList);
                } catch (AbnormalModelException e) {
                    throw new InvalidRequestException();
                }
            }
        } catch (TodoNotFoundException | AbnormalModelException e) {
            throw new InvalidRequestException();
        }
    }

    public void move(User user, String localIdentifier, String targetLocalIdentifier) throws InvalidRequestException {
        MasterList masterList = masterListRepository.find(user.getIdentifier())
            .orElseThrow(InvalidRequestException::new);
        try {
            masterList.move(localIdentifier, targetLocalIdentifier);
            masterListRepository.save(masterList);
        } catch (TodoNotFoundException | AbnormalModelException e) {
            throw new InvalidRequestException();
        }
    }

    public void pull(User user) throws InvalidRequestException {
        MasterList masterList = masterListRepository.find(user.getIdentifier())
            .orElseThrow(InvalidRequestException::new);
        try {
            masterList.pull();
            masterListRepository.save(masterList);
        } catch (AbnormalModelException e) {
            throw new InvalidRequestException();
        }
    }
}
