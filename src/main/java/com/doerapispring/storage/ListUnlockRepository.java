package com.doerapispring.storage;

import com.doerapispring.domain.AbnormalModelException;
import com.doerapispring.domain.AggregateRootRepository;
import com.doerapispring.domain.ListUnlock;
import com.doerapispring.domain.MasterList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;

@Repository
@Transactional
class ListUnlockRepository implements AggregateRootRepository<MasterList, ListUnlock> {
    private final ListUnlockDao listUnlockDao;
    private final UserDAO userDAO;

    @Autowired
    ListUnlockRepository(ListUnlockDao listUnlockDao, UserDAO userDAO) {
        this.listUnlockDao = listUnlockDao;
        this.userDAO = userDAO;
    }

    @Override
    public void add(MasterList masterList, ListUnlock listUnlock) throws AbnormalModelException {
        UserEntity userEntity = userDAO.findByEmail(masterList.getIdentifier().get());
        if (userEntity == null) throw new AbnormalModelException();
        ListUnlockEntity listUnlockEntity = ListUnlockEntity.builder()
                .userEntity(userEntity)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        listUnlockDao.save(listUnlockEntity);
    }
}
