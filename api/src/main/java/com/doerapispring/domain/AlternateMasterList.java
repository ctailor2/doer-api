package com.doerapispring.domain;

import java.time.Clock;
import java.util.*;
import java.util.stream.Collectors;

class AlternateMasterList implements IMasterList {
    static final String NAME = "now";
    private static final String DEFERRED_NAME = "later";
    private final List<Todo> todos = new ArrayList<>();
    private final Clock clock;
    private final UniqueIdentifier<String> uniqueIdentifier;
    private final List<ListUnlock> listUnlocks;
    private int listDemarcationIndex = 0;

    AlternateMasterList(Clock clock, UniqueIdentifier<String> uniqueIdentifier, List<ListUnlock> listUnlocks) {
        this.clock = clock;
        this.uniqueIdentifier = uniqueIdentifier;
        this.listUnlocks = listUnlocks;
    }

    @Override
    public Todo add(String task) throws ListSizeExceededException, DuplicateTodoException {
        if (todos.stream().anyMatch(todo -> todo.getTask().equals(task))) {
            throw new DuplicateTodoException();
        }
        if (getTodos().size() >= maxSize()) {
            throw new ListSizeExceededException();
        }
        int position;
        if (todos.isEmpty()) {
            position = 1;
        } else {
            position = todos.get(0).getPosition() - 1;
        }
        Todo todo = new Todo(UUID.randomUUID().toString(), task, MasterList.NAME, position);
        todos.add(0, todo);
        listDemarcationIndex++;
        return todo;
    }

    @Override
    public List<Todo> getTodos() {
        return todos.subList(0, listDemarcationIndex);
    }

    @Override
    public Todo addDeferred(String task) throws DuplicateTodoException {
        if (todos.stream().anyMatch(todo -> todo.getTask().equals(task))) {
            throw new DuplicateTodoException();
        }
        int position;
        if (todos.isEmpty()) {
            position = 1;
        } else {
            position = todos.get(todos.size() - 1).getPosition() + 1;
        }
        Todo todo = new Todo(UUID.randomUUID().toString(), task, MasterList.DEFERRED_NAME, position);
        todos.add(todo);
        return todo;
    }

    @Override
    public ListUnlock unlock() throws LockTimerNotExpiredException {
        if (!isAbleToBeUnlocked()) {
            throw new LockTimerNotExpiredException();
        }
        ListUnlock listUnlock = new ListUnlock(Date.from(clock.instant()));
        listUnlocks.add(listUnlock);
        return listUnlock;
    }

    @Override
    public List<Todo> getDeferredTodos() throws LockTimerNotExpiredException {
        if (isLocked()) {
            throw new LockTimerNotExpiredException();
        }
        return deferredTodos();
    }

    @Override
    public Todo delete(String localIdentifier) throws TodoNotFoundException {
        Todo todoToDelete = getByLocalIdentifier(localIdentifier);
        if (todos.indexOf(todoToDelete) < listDemarcationIndex) {
            listDemarcationIndex--;
        }
        todos.remove(todoToDelete);
        return todoToDelete;
    }

    @Override
    public Todo displace(String localIdentifier, String task) throws TodoNotFoundException, DuplicateTodoException {
//        1 task1
//        2 task2
//        ----
//        3 task3

//        Suppose the first task is displaced
//        It is moved to right before the listDemarcationIndex

//        2 task2
//        1 task1
//        ----
//        3 task3

//        And positions are updated accordingly

//        1 task2
//        2 task1
//        ----
//        3 task3

//        The listDemarcationIndex is decremented

//        1 task2
//        ----
//        2 task1
//        3 task3

//        And a new task is added (before the first task)

//        0 displacingTask
//        1 task2
//        ----
//        2 task1
//        3 task3

//        This would necessitate changing add to always add before
//        And addDeferred to always add after
        String targetLocalIdentifier;
        if (!getTodos().isEmpty()) {
            targetLocalIdentifier = getTodos().get(listDemarcationIndex - 1).getLocalIdentifier();
        } else {
            targetLocalIdentifier = localIdentifier;
        }
        move(localIdentifier, targetLocalIdentifier);
        Todo todo = todos.get(listDemarcationIndex - 1);
        listDemarcationIndex--;
        todo.setListName(DEFERRED_NAME);
        try {
            return add(task);
        } catch (ListSizeExceededException e) {
            throw new RuntimeException("This should never happen");
        }
    }

    @Override
    public Todo update(String localIdentifier, String task) throws TodoNotFoundException, DuplicateTodoException {
        if (todos.stream().anyMatch(todo -> todo.getTask().equals(task))) {
            throw new DuplicateTodoException();
        }
        Todo todo = getByLocalIdentifier(localIdentifier);
        todo.setTask(task);
        return todo;
    }

    @Override
    public Todo complete(String localIdentifier) throws TodoNotFoundException {
        Todo todo = delete(localIdentifier);
        todo.complete();
        return todo;
    }

    @Override
    public List<Todo> move(String localIdentifier, String targetLocalIdentifier) throws TodoNotFoundException {
        Todo todo = getByLocalIdentifier(localIdentifier);
        int index = todos.indexOf(todo);
        Todo targetTodo = getByLocalIdentifier(targetLocalIdentifier);
        int targetIndex;
        List<Todo> subList;
        if (index < listDemarcationIndex) {
            subList = getTodos();
            targetIndex = subList.indexOf(targetTodo);
        } else {
            subList = deferredTodos();
            targetIndex = subList.indexOf(targetTodo);
        }

        Direction direction = Direction.valueOf(Integer.compare(targetTodo.getPosition(), todo.getPosition()));
        Map<Integer, Integer> originalMapping = new HashMap<>();
        for (int i = index; direction.targetNotExceeded(i, targetIndex); i += direction.getValue()) {
            originalMapping.put(i, subList.get(i).getPosition());
        }

        subList.remove(todo);
        subList.add(targetIndex, todo);

        return originalMapping.entrySet().stream()
            .map(entry -> {
                Todo effectedTodo = subList.get(entry.getKey());
                effectedTodo.setPosition(entry.getValue());
                return effectedTodo;
            })
            .collect(Collectors.toList());
    }

    @Override
    public boolean isAbleToBeUnlocked() {
        return isLocked() && mostRecentListUnlock()
            .map(listUnlock -> listUnlock.before(beginningOfToday()))
            .orElse(true);
    }

    @Override
    public boolean isLocked() {
        return mostRecentListUnlock()
            .map(listUnlock -> listUnlock.before(Date.from(clock.instant().minusSeconds(1800L))))
            .orElse(true);
    }

    @Override
    public Long unlockDuration() {
        return mostRecentListUnlock()
            .map(listUnlock -> listUnlock.toInstant().toEpochMilli() + 1800000L - clock.instant().toEpochMilli())
            .filter(duration -> duration > 0L)
            .orElse(0L);
    }

    @Override
    public List<Todo> pull() {
        List<Todo> pulledTodos = new ArrayList<>();
        while (listDemarcationIndex < todos.size() && getTodos().size() < maxSize()) {
            Todo pulledTodo = todos.get(listDemarcationIndex);
            pulledTodo.setListName(AlternateMasterList.NAME);
            pulledTodos.add(pulledTodo);
            listDemarcationIndex++;
        }
        return pulledTodos;
    }

    @Override
    public boolean isFull() {
        return getTodos().size() >= maxSize();
    }

    @Override
    public boolean isAbleToBeReplenished() {
        return !isFull() && deferredTodos().size() > 0;
    }

    private Todo getByLocalIdentifier(String localIdentifier) throws TodoNotFoundException {
        return todos.stream()
            .filter(todo -> localIdentifier.equals(todo.getLocalIdentifier()))
            .findFirst()
            .orElseThrow(TodoNotFoundException::new);
    }

    private List<Todo> deferredTodos() {
        return todos.subList(listDemarcationIndex, todos.size());
    }

    private int maxSize() {
        return 2;
    }

    private Date beginningOfToday() {
        Date now = Date.from(clock.instant());
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeZone(TimeZone.getTimeZone(clock.getZone()));
        calendar.setTime(now);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar.getTime();
    }

    private Optional<Date> mostRecentListUnlock() {
        return listUnlocks.stream()
            .findFirst()
            .map(ListUnlock::getCreatedAt);
    }
}
