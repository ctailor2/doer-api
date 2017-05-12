package com.doerapispring.domain;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class TodoList {
    private final ScheduledFor scheduling;
    private List<Todo> todos = new ArrayList<>();
    private final int maxSize;

    public TodoList(ScheduledFor scheduling, List<Todo> todos, int maxSize) {
        this.scheduling = scheduling;
        this.maxSize = maxSize;
        this.todos.addAll(todos);
    }

    public ScheduledFor getScheduling() {
        return scheduling;
    }

    int getMaxSize() {
        return maxSize;
    }

    public List<Todo> getTodos() {
        return todos;
    }

    public boolean isFull() {
        return maxSize >= 0 && todos.size() >= maxSize;
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
        todos.stream().forEach(this::remove);
        return todos;
    }

    void replace(Todo existingTodo, Todo replacementTodo) {
        int indexOfExistingTodo = todos.indexOf(existingTodo);
        todos.set(indexOfExistingTodo, replacementTodo);
    }

    @Override
    public String toString() {
        return "TodoList{" +
                "scheduling=" + scheduling +
                ", todos=" + todos +
                ", maxSize=" + maxSize +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        TodoList todoList = (TodoList) o;

        if (maxSize != todoList.maxSize) return false;
        if (scheduling != todoList.scheduling) return false;
        if (todos != null ? !todos.equals(todoList.todos) : todoList.todos != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = scheduling != null ? scheduling.hashCode() : 0;
        result = 31 * result + (todos != null ? todos.hashCode() : 0);
        result = 31 * result + maxSize;
        return result;
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
