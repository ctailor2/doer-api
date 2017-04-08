package com.doerapispring.domain;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

public class MasterList implements UniquelyIdentifiable<String> {
    private final UniqueIdentifier<String> uniqueIdentifier;
    private final Integer focusSize;
    private final TodoList immediateList;
    private final TodoList postponedList;

    public MasterList(UniqueIdentifier uniqueIdentifier,
                      int focusSize) {
        this.uniqueIdentifier = uniqueIdentifier;
        this.focusSize = focusSize;
        immediateList = new TodoList(ScheduledFor.now);
        postponedList = new TodoList(ScheduledFor.later);
    }

    public MasterList(UniqueIdentifier<String> uniqueIdentifier,
                      int focusSize,
                      List<Todo> todos) {
        this.uniqueIdentifier = uniqueIdentifier;
        this.focusSize = focusSize;
        Map<Boolean, List<Todo>> partitionedTodos = todos.stream()
                .collect(Collectors.partitioningBy(todo ->
                        ScheduledFor.now.equals(todo.getScheduling())));
        immediateList = new TodoList(ScheduledFor.now, partitionedTodos.get(true));
        postponedList = new TodoList(ScheduledFor.later, partitionedTodos.get(false));
    }

    public List<Todo> getTodos() {
        ArrayList<Todo> todos = new ArrayList<>();
        todos.addAll(immediateList.todos);
        todos.addAll(postponedList.todos);
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
        if (getByTask(task).isPresent()) throw new DuplicateTodoException();
        return getListForScheduling(scheduling).add(task);
    }

    public List<Todo> pull() {
        TodoList laterList = getListForScheduling(ScheduledFor.later);
        TodoList nowList = getListForScheduling(ScheduledFor.now);
        List<Todo> laterTodos = laterList.pop(focusSize - nowList.todos.size());
        return laterTodos.stream()
                .map(todo ->
                        nowList.addExisting(todo.getLocalIdentifier(), todo.getTask()))
                .collect(Collectors.toList());
    }

    private TodoList getListForScheduling(ScheduledFor scheduling) {
        if (ScheduledFor.now.equals(scheduling)) {
            return immediateList;
        }
        return postponedList;
    }

    private Todo getByLocalIdentifier(String localIdentifier) throws TodoNotFoundException {
        return getTodos().stream()
                .filter(todo -> localIdentifier.equals(todo.getLocalIdentifier()))
                .findFirst()
                .orElseThrow(TodoNotFoundException::new);
    }

    private Optional<Todo> getByTask(String task) {
        return getTodos().stream().filter(todo ->
                todo.getTask().equals(task))
                .findFirst();
    }

    public Todo delete(String localIdentifier) throws TodoNotFoundException {
        Todo todoToDelete = getByLocalIdentifier(localIdentifier);
        getListForScheduling(todoToDelete.getScheduling()).remove(todoToDelete);
        return todoToDelete;
    }

    public List<Todo> displace(String localIdentifier, String task) throws TodoNotFoundException, DuplicateTodoException {
        if (getByTask(task).isPresent()) throw new DuplicateTodoException();
        Todo existingTodo = getByLocalIdentifier(localIdentifier);
        Todo todoCopy = getListForScheduling(ScheduledFor.later).push(existingTodo.getTask());
        existingTodo.setTask(task);
        return asList(todoCopy, existingTodo);
    }

    public Todo update(String localIdentifier, String task) throws TodoNotFoundException, DuplicateTodoException {
        if (getByTask(task).isPresent()) throw new DuplicateTodoException();
        Todo existingTodo = getByLocalIdentifier(localIdentifier);
        existingTodo.setTask(task);
        return existingTodo;
    }

    public Todo complete(String localIdentifier) throws TodoNotFoundException {
        Todo existingTodo = getByLocalIdentifier(localIdentifier);
        getListForScheduling(existingTodo.getScheduling()).remove(existingTodo);
        existingTodo.complete();
        return existingTodo;
    }

    public boolean isImmediateListFull() {
        return focusSize.equals(immediateList.size());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MasterList that = (MasterList) o;

        if (uniqueIdentifier != null ? !uniqueIdentifier.equals(that.uniqueIdentifier) : that.uniqueIdentifier != null)
            return false;
        if (focusSize != null ? !focusSize.equals(that.focusSize) : that.focusSize != null) return false;
        if (immediateList != null ? !immediateList.equals(that.immediateList) : that.immediateList != null)
            return false;
        return postponedList != null ? postponedList.equals(that.postponedList) : that.postponedList == null;

    }

    @Override
    public int hashCode() {
        int result = uniqueIdentifier != null ? uniqueIdentifier.hashCode() : 0;
        result = 31 * result + (focusSize != null ? focusSize.hashCode() : 0);
        result = 31 * result + (immediateList != null ? immediateList.hashCode() : 0);
        result = 31 * result + (postponedList != null ? postponedList.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "MasterList{" +
                "uniqueIdentifier=" + uniqueIdentifier +
                ", focusSize=" + focusSize +
                ", immediateList=" + immediateList +
                ", postponedList=" + postponedList +
                '}';
    }

    public List<Todo> move(String originalTodoIdentifier, String targetTodoIdentifier) throws TodoNotFoundException {
        Todo originalTodo = getByLocalIdentifier(originalTodoIdentifier);
        TodoList todoList = getListForScheduling(originalTodo.getScheduling());
        Todo targetTodo = todoList.getByIdentifier(targetTodoIdentifier);
        return todoList.move(originalTodo, targetTodo);
    }

    private static class TodoList {
        private final ScheduledFor scheduling;
        private List<Todo> todos = new ArrayList<>();

        TodoList(ScheduledFor scheduling) {
            this.scheduling = scheduling;
        }

        TodoList(ScheduledFor scheduling, List<Todo> todos) {
            this.scheduling = scheduling;
            this.todos.addAll(todos);
        }

        Todo add(String task) {
            int position = getNextPosition();
            Todo todo = new Todo(task, scheduling, position);
            todos.add(todo);
            return todo;
        }

        Todo addExisting(String localIdentifier, String task) {
            int position = getNextPosition();
            Todo newTodo = new Todo(localIdentifier, task, scheduling, position);
            todos.add(newTodo);
            return newTodo;
        }

        Todo push(String task) {
            int position = getNextTopPosition();
            Todo todo = new Todo(task, scheduling, position);
            todos.add(0, todo);
            return todo;
        }

        private Integer getNextPosition() {
            if (isEmpty()) {
                return 1;
            }
            return todos.get(todos.size() - 1).getPosition() + 1;
        }

        private Integer getNextTopPosition() {
            if (isEmpty()) {
                return 1;
            }
            return todos.get(0).getPosition() - 1;
        }

        private boolean isEmpty() {
            return size() == 0;
        }

        private Integer size() {
            return todos.size();
        }

        private void remove(Todo todo) {
            todos.remove(todo);
        }

        private List<Todo> move(Todo originalTodo, Todo targetTodo) {
            Direction direction = Direction.valueOf(Integer.compare(targetTodo.getPosition(), originalTodo.getPosition()));

            int originalIndex = todos.indexOf(originalTodo);
            int targetIndex = todos.indexOf(targetTodo);

            Map<Integer, Integer> originalMapping = new HashMap<>();
            for (int index = originalIndex; direction.targetNotExceeded(index, targetIndex); index += direction.getValue()) {
                originalMapping.put(index, todos.get(index).getPosition());
            }

            remove(originalTodo);
            todos.add(targetIndex, originalTodo);

            return originalMapping.entrySet().stream()
                    .map(entry -> {
                        Todo todo = this.todos.get(entry.getKey());
                        todo.setPosition(entry.getValue());
                        return todo;
                    })
                    .collect(Collectors.toList());
        }

        private Todo getByIdentifier(String targetTodoIdentifier) throws TodoNotFoundException {
            return todos.stream()
                    .filter(todo -> targetTodoIdentifier.equals(todo.getLocalIdentifier()))
                    .findFirst()
                    .orElseThrow(TodoNotFoundException::new);
        }

        private List<Todo> pop(Integer count) {
            List<Todo> todos = this.todos.stream()
                    .limit(count)
                    .collect(Collectors.toList());
            todos.stream().forEach(this::remove);
            return todos;
        }
    }

    private enum Direction {
        UP(-1),
        DOWN(1),
        NONE(0);

        private static Map<Integer, Direction> valueMapping = new HashMap<>();

        static {
            for (Direction direction : Direction.values()) {
                valueMapping.put(direction.value, direction);
            }
        }

        private final int value;

        Direction(int value) {
            this.value = value;
        }

        public static Direction valueOf(int directionValue) {
            return valueMapping.get(directionValue);
        }

        public boolean targetNotExceeded(int currentIndex, int targetIndex) {
            switch (this) {
                case UP:
                    return currentIndex >= targetIndex;
                case DOWN:
                    return currentIndex <= targetIndex;
                case NONE:
                    return false;
                default:
                    return false;
            }
        }

        public int getValue() {
            return value;
        }
    }
}
