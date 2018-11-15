package com.doerapispring.storage;

import com.doerapispring.domain.IdentityGeneratingRepository;
import com.doerapispring.domain.TodoId;
import org.springframework.stereotype.Repository;
import org.springframework.util.IdGenerator;

@Repository
public class TodoRepository implements IdentityGeneratingRepository<TodoId> {
    private final IdGenerator idGenerator;

    public TodoRepository(IdGenerator idGenerator) {
        this.idGenerator = idGenerator;
    }

    @Override
    public TodoId nextIdentifier() {
        return new TodoId(idGenerator.generateId().toString());
    }
}
