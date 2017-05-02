package com.doerapispring.storage;

import com.doerapispring.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Transactional
class ListUnlockRepository implements AggregateRootRepository<ListManager, ListUnlock, String> {
    private final ListUnlockDao listUnlockDao;
    private final UserDAO userDAO;

    @Autowired
    ListUnlockRepository(ListUnlockDao listUnlockDao, UserDAO userDAO) {
        this.listUnlockDao = listUnlockDao;
        this.userDAO = userDAO;
    }

    @Override
    public Optional<ListManager> find(UniqueIdentifier<String> uniqueIdentifier) {
        String email = uniqueIdentifier.get();
        List<ListUnlockEntity> listViewEntities = listUnlockDao.findUserListView(email);
        List<ListUnlock> listUnlocks = listViewEntities.stream()
                .map(listViewEntity -> new ListUnlock(listViewEntity.updatedAt))
                .collect(Collectors.toList());
        ListManager listViewManager = new ListManager(uniqueIdentifier, listUnlocks);
        return Optional.of(listViewManager);
    }

    @Override
    public void add(ListManager listManager, ListUnlock listUnlock) throws AbnormalModelException {
        UserEntity userEntity = userDAO.findByEmail(listManager.getIdentifier().get());
        if (userEntity == null) throw new AbnormalModelException();
        ListUnlockEntity listUnlockEntity = ListUnlockEntity.builder()
                .userEntity(userEntity)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        listUnlockDao.save(listUnlockEntity);
    }
}
