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
class ListViewRepository implements AggregateRootRepository<ListViewManager, ListView, String> {
    private final ListViewDao listViewDao;
    private final UserDAO userDAO;

    @Autowired
    ListViewRepository(ListViewDao listViewDao, UserDAO userDAO) {
        this.listViewDao = listViewDao;
        this.userDAO = userDAO;
    }

    @Override
    public Optional<ListViewManager> find(UniqueIdentifier<String> uniqueIdentifier) {
        String email = uniqueIdentifier.get();
        List<ListViewEntity> listViewEntities = listViewDao.findUserListView(email);
        List<ListView> listViews = listViewEntities.stream()
                .map(listViewEntity -> new ListView(listViewEntity.updatedAt))
                .collect(Collectors.toList());
        ListViewManager listViewManager = new ListViewManager(uniqueIdentifier, listViews);
        return Optional.of(listViewManager);
    }

    @Override
    public void add(ListViewManager listViewManager, ListView listView) throws AbnormalModelException {
        UserEntity userEntity = userDAO.findByEmail(listViewManager.getIdentifier().get());
        if (userEntity == null) throw new AbnormalModelException();
        ListViewEntity listViewEntity = ListViewEntity.builder()
                .userEntity(userEntity)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();
        listViewDao.save(listViewEntity);
    }
}
