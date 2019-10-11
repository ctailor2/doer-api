package com.doerapispring.storage;

import com.doerapispring.domain.ListId;
import com.doerapispring.domain.ListOverview;
import com.doerapispring.domain.OwnedObjectRepository;
import com.doerapispring.domain.UserId;
import org.springframework.stereotype.Repository;
import org.springframework.util.IdGenerator;

import java.time.Instant;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

@Repository
public class ListOverviewRepository implements OwnedObjectRepository<ListOverview, UserId, ListId> {
    private final UserDAO userDAO;
    private final TodoListDao todoListDao;
    private IdGenerator idGenerator;

    ListOverviewRepository(UserDAO userDAO,
                           TodoListDao todoListDao,
                           IdGenerator idGenerator) {
        this.userDAO = userDAO;
        this.todoListDao = todoListDao;
        this.idGenerator = idGenerator;
    }

    @Override
    public void save(ListOverview listOverview) {
        UserEntity userEntity = userDAO.findByEmail(listOverview.getUserId().get());
        TodoListEntity todoListEntity = TodoListEntity.builder()
            .userEntity(userEntity)
            .uuid(listOverview.getListId().get())
            .name(listOverview.getName())
            .demarcationIndex(listOverview.getDemarcationIndex())
            .lastUnlockedAt(listOverview.getLastUnlockedAt())
            .build();
        todoListDao.save(todoListEntity);
    }

    @Override
    public ListId nextIdentifier() {
        return new ListId(idGenerator.generateId().toString());
    }

    @Override
    public List<ListOverview> findAll(UserId userId) {
        return todoListDao.findAllOverviews(userId.get()).stream()
            .map(todoListEntity -> new ListOverview(userId, new ListId(todoListEntity.uuid), todoListEntity.name, 0, Date.from(Instant.EPOCH)))
            .collect(Collectors.toList());
    }
}
