package com.doerapispring.storage;

import com.doerapispring.domain.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
class MasterListRepository implements DomainRepository<MasterList, String> {
    private final TodoDao todoDao;

    @Autowired
    MasterListRepository(TodoDao todoDao) {
        this.todoDao = todoDao;
    }

    @Override
    public Optional<MasterList> find(UniqueIdentifier<String> uniqueIdentifier) {
        String email = uniqueIdentifier.get();
        List<TodoEntity> todoEntities = todoDao.findByUserEmail(email);
        if (todoEntities.size() == 0) return Optional.empty();
        Map<Boolean, List<Todo>> partitionedTodos = todoEntities.stream()
                .map(todoEntity -> new Todo(
                        new UserIdentifier(todoEntity.userEntity.email),
                        todoEntity.task,
                        todoEntity.active ? ScheduledFor.now : ScheduledFor.later))
                .collect(Collectors.partitioningBy(todo -> todo.getScheduling() == ScheduledFor.now));
        return Optional.of(new MasterList(new ImmediateList(partitionedTodos.get(true)),
                new PostponedList(partitionedTodos.get(false))));
    }
}
