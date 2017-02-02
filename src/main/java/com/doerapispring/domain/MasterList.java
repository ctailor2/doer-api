package com.doerapispring.domain;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class MasterList implements UniquelyIdentifiable<String> {
    private final UniqueIdentifier<String> uniqueIdentifier;
    private final Integer focusSize;
    private final ArrayList<Todo> todos = new ArrayList<>();

    public MasterList(UniqueIdentifier uniqueIdentifier,
                      int focusSize) {
        this.uniqueIdentifier = uniqueIdentifier;
        this.focusSize = focusSize;
    }

    static public MasterList newEmpty(UniqueIdentifier uniqueIdentifier) {
        return new MasterList(uniqueIdentifier, 2);
    }

    public List<Todo> getTodos() {
        return todos;
    }

    @Override
    public UniqueIdentifier<String> getIdentifier() {
        return uniqueIdentifier;
    }

    public Todo add(String task, ScheduledFor scheduling) throws ListSizeExceededException, DuplicateTodoException {
        if (ScheduledFor.now.equals(scheduling) && isImmediateListFull()) {
            throw new ListSizeExceededException();
        }
        if (getByTask(task, scheduling).isPresent()) throw new DuplicateTodoException();
        Integer indexToInsert = nextIndexForScheduling(scheduling);
        Todo todo = new Todo(indexToInsert.toString(), task, scheduling);
        todos.add(indexToInsert, todo);
        return todo;
    }

    private Optional<Todo> getByTask(String task, ScheduledFor scheduling) {
        return todos.stream().filter(todo ->
                todo.getTask().equals(task) && todo.getScheduling().equals(scheduling))
                .findFirst();
    }

    private Integer nextIndexForScheduling(ScheduledFor scheduling) {
        if (ScheduledFor.now.equals(scheduling)) {
            return nextNowIndex();
        }
        return nextLaterIndex();
    }

    private Integer nextNowIndex() {
        List<Todo> immediateTodos = getNowList();
        if (immediateTodos.isEmpty()) {
            return 0;
        } else {
            return todos.indexOf(immediateTodos.get(immediateTodos.size() - 1)) + 1;
        }
    }

    private Integer nextLaterIndex() {
        List<Todo> postponedTodos = getLaterList();
        if (postponedTodos.isEmpty()) {
            return todos.size();
        } else {
            return todos.indexOf(postponedTodos.get(postponedTodos.size() - 1)) + 1;
        }
    }

    private List<Todo> getNowList() {
        return todos.stream()
                .filter(todo -> ScheduledFor.now.equals(todo.getScheduling()))
                .collect(Collectors.toList());
    }

    private List<Todo> getLaterList() {
        return todos.stream()
                .filter(todo -> ScheduledFor.later.equals(todo.getScheduling()))
                .collect(Collectors.toList());
    }

    public boolean isImmediateListFull() {
        return focusSize.equals(getNowList().size());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MasterList that = (MasterList) o;

        if (uniqueIdentifier != null ? !uniqueIdentifier.equals(that.uniqueIdentifier) : that.uniqueIdentifier != null)
            return false;
        if (focusSize != null ? !focusSize.equals(that.focusSize) : that.focusSize != null) return false;
        return todos != null ? todos.equals(that.todos) : that.todos == null;

    }

    @Override
    public int hashCode() {
        int result = uniqueIdentifier != null ? uniqueIdentifier.hashCode() : 0;
        result = 31 * result + (focusSize != null ? focusSize.hashCode() : 0);
        result = 31 * result + (todos != null ? todos.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MasterList{" +
                "uniqueIdentifier=" + uniqueIdentifier +
                ", focusSize=" + focusSize +
                ", todos=" + todos +
                '}';
    }

    public Todo delete(String localIdentifier) throws TodoNotFoundException {
        Todo todoToDelete = todos.stream()
                .filter(todo -> localIdentifier.equals(todo.getLocalIdentifier()))
                .findFirst()
                .orElseThrow(TodoNotFoundException::new);
        todos.remove(todoToDelete);
        return todoToDelete;
    }
}
