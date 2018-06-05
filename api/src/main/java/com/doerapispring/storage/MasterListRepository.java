package com.doerapispring.storage;

import com.doerapispring.domain.MasterList;
import com.doerapispring.domain.ObjectRepository;
import com.doerapispring.domain.Todo;
import com.doerapispring.domain.UniqueIdentifier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.time.Clock;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
class MasterListRepository implements ObjectRepository<MasterList, String> {
    private final Clock clock;
    private final TodoDao todoDao;
    private final ListUnlockDao listUnlockDao;

    @Autowired
    MasterListRepository(Clock clock, TodoDao todoDao, ListUnlockDao listUnlockDao) {
        this.clock = clock;
        this.todoDao = todoDao;
        this.listUnlockDao = listUnlockDao;
    }

    @Override
    public Optional<MasterList> find(UniqueIdentifier<String> uniqueIdentifier) {
        String email = uniqueIdentifier.get();
        List<TodoEntity> todoEntities = todoDao.findUnfinishedByUserEmail(email);
        Map<Boolean, List<Todo>> partitionedTodos = todoEntities.stream()
            .map(todoEntity -> new Todo(
                todoEntity.uuid,
                todoEntity.task,
                todoEntity.active ? MasterList.NAME : MasterList.DEFERRED_NAME,
                todoEntity.position))
            .collect(Collectors.partitioningBy(todo -> MasterList.NAME.equals(todo.getListName())));
        ListUnlockEntity listUnlockEntity = listUnlockDao.findFirstUserListUnlock(email);
        Date lastUnlocked = listUnlockEntity != null ? listUnlockEntity.updatedAt : null;
        MasterList masterList = new MasterList(clock, uniqueIdentifier, partitionedTodos.get(true), partitionedTodos.get(false), lastUnlocked);
        return Optional.of(masterList);
    }
}
