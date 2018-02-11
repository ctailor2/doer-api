package com.doerapispring.domain;

import java.util.*;
import java.util.stream.Collectors;

import static java.util.Comparator.comparingInt;

public class TodoList {
    private final String name;
    private final List<Todo> todos = new ArrayList<>();
    private final int maxSize;

    public TodoList(String name, List<Todo> todos, int maxSize) {
        this.name = name;
        this.maxSize = maxSize;
        this.todos.addAll(todos);
    }

    TodoList(String name, int maxSize) {
        this.name = name;
        this.maxSize = maxSize;
    }

    String getName() {
        return name;
    }

    List<Todo> getTodos() {
        return todos;
    }

    int getMaxSize() {
        return maxSize;
    }

    boolean isFull() {
        return maxSize >= 0 && todos.size() >= maxSize;
    }

    Todo add(String task) throws ListSizeExceededException {
        if (isFull()) {
            throw new ListSizeExceededException();
        }
        Todo todo = new Todo(
            generateIdentifier(),
            task,
            name,
            getNextPosition());
        todos.add(todo);
        return todo;
    }

    Todo add(String task, Integer position) throws ListSizeExceededException {
        if (isFull()) {
            throw new ListSizeExceededException();
        }
        Todo todo = new Todo(
            generateIdentifier(),
            task,
            name,
            position);
        todos.add(todo);
        todos.sort(comparingInt(Todo::getPosition) );
        return todo;
    }

    Todo addExisting(String localIdentifier, String task) throws ListSizeExceededException {
        if (isFull()) {
            throw new ListSizeExceededException();
        }
        int position = getNextPosition();
        Todo newTodo = new Todo(localIdentifier, task, name, position);
        todos.add(newTodo);
        return newTodo;
    }

    void remove(Todo todo) {
        todos.remove(todo);
    }

    List<Todo> move(Todo originalTodo, Todo targetTodo) {
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

    Todo getByIdentifier(String targetTodoIdentifier) throws TodoNotFoundException {
        return todos.stream()
            .filter(todo -> targetTodoIdentifier.equals(todo.getLocalIdentifier()))
            .findFirst()
            .orElseThrow(TodoNotFoundException::new);
    }

    List<Todo> pop(Integer count) {
        List<Todo> todos = this.todos.stream()
            .limit(count)
            .collect(Collectors.toList());
        todos.forEach(this::remove);
        return todos;
    }

    void pushExisting(String localIdentifier, String task) {
        int position = getNextTopPosition();
        Todo todo = new Todo(localIdentifier, task, name, position);
        todos.add(0, todo);
    }

    private String generateIdentifier() {
        return UUID.randomUUID().toString();
    }

    Integer size() {
        return todos.size();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TodoList todoList = (TodoList) o;

        if (maxSize != todoList.maxSize) return false;
        if (name != null ? !name.equals(todoList.name) : todoList.name != null) return false;
        return todos != null ? todos.equals(todoList.todos) : todoList.todos == null;
    }

    @Override
    public int hashCode() {
        int result = name != null ? name.hashCode() : 0;
        result = 31 * result + (todos != null ? todos.hashCode() : 0);
        result = 31 * result + maxSize;
        return result;
    }

    @Override
    public String toString() {
        return "TodoList{" +
            "name='" + name + '\'' +
            ", todos=" + todos +
            ", maxSize=" + maxSize +
            '}';
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

    private enum Direction {
        UP(-1),
        DOWN(1),
        NONE(0);

        private static final Map<Integer, Direction> valueMapping = new HashMap<>();

        static {
            for (Direction direction : Direction.values()) {
                valueMapping.put(direction.value, direction);
            }
        }

        private final int value;

        Direction(int value) {
            this.value = value;
        }

        static Direction valueOf(int directionValue) {
            return valueMapping.get(directionValue);
        }

        boolean targetNotExceeded(int currentIndex, int targetIndex) {
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

        int getValue() {
            return value;
        }
    }
}
