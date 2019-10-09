package com.doerapispring.storage;

import com.doerapispring.domain.ListId;
import com.doerapispring.domain.ListOverview;
import com.doerapispring.domain.OwnedObjectRepository;
import com.doerapispring.domain.UserId;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ListOverviewRepository implements OwnedObjectRepository<ListOverview, UserId, ListId> {
    private final TodoListDao todoListDao;

    ListOverviewRepository(TodoListDao todoListDao) {
        this.todoListDao = todoListDao;
    }

    @Override
    public void save(ListOverview model) {
    }

    @Override
    public ListId nextIdentifier() {
        return null;
    }

    @Override
    public List<ListOverview> findAll(UserId userId) {
        return todoListDao.findAllOverviews(userId.get()).stream()
            .map(todoListEntity -> new ListOverview(new ListId(todoListEntity.uuid), todoListEntity.name))
            .collect(Collectors.toList());
    }
}
