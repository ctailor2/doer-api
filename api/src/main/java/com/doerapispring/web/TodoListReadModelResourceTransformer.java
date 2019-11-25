package com.doerapispring.web;

import com.doerapispring.domain.TodoListReadModel;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

@Component
class TodoListReadModelResourceTransformer {
    private final HateoasLinkGenerator hateoasLinkGenerator;

    public TodoListReadModelResourceTransformer(HateoasLinkGenerator hateoasLinkGenerator) {
        this.hateoasLinkGenerator = hateoasLinkGenerator;
    }

    TodoListReadModelResponse getTodoListReadModelResponse(TodoListReadModel todoListReadModel) {
        TodoListReadModelDTO todoListReadModelDTO = new TodoListReadModelDTO(
            todoListReadModel.getProfileName(),
            todoListReadModel.getSectionName(),
            todoListReadModel.getDeferredSectionName(),
            todoListReadModel.getTodos().stream()
                .map(todo -> new TodoDTO(
                    todo.getTodoId().getIdentifier(),
                    todo.getTask()))
                .collect(Collectors.toList()),
            todoListReadModel.getDeferredTodos().stream()
                .map(todo -> new TodoDTO(
                    todo.getTodoId().getIdentifier(),
                    todo.getTask()))
                .collect(Collectors.toList()),
            todoListReadModel.unlockDuration()
        );
        String listId = todoListReadModel.getListId().get();
        todoListReadModelDTO.add(hateoasLinkGenerator.createDeferredTodoLink(listId).withRel("createDeferred"));
        if (todoListReadModel.isAbleToBeUnlocked()) {
            todoListReadModelDTO.add(hateoasLinkGenerator.listUnlockLink(listId).withRel("unlock"));
        }
        if (todoListReadModel.isFull()) {
            todoListReadModelDTO.add(hateoasLinkGenerator.displaceTodoLink(listId).withRel("displace"));
        } else {
            todoListReadModelDTO.add(hateoasLinkGenerator.createTodoLink(listId).withRel("create"));
        }
        if (todoListReadModel.isAbleToBeReplenished()) {
            todoListReadModelDTO.add(hateoasLinkGenerator.listPullTodosLink(listId).withRel("pull"));
        }
        if (todoListReadModel.isAbleToBeEscalated()) {
            todoListReadModelDTO.add(hateoasLinkGenerator.listEscalateTodoLink(listId).withRel("escalate"));
        }
        todoListReadModelDTO.getTodos().forEach(todoDTO -> {
            todoDTO.add(hateoasLinkGenerator.deleteTodoLink(listId, todoDTO.getIdentifier()).withRel("delete"));
            todoDTO.add(hateoasLinkGenerator.updateTodoLink(listId, todoDTO.getIdentifier()).withRel("update"));
            todoDTO.add(hateoasLinkGenerator.completeTodoLink(listId, todoDTO.getIdentifier()).withRel("complete"));

            todoListReadModelDTO.getTodos().forEach(targetTodoDTO ->
                todoDTO.add(hateoasLinkGenerator.moveTodoLink(
                    listId,
                    todoDTO.getIdentifier(), targetTodoDTO.getIdentifier()).withRel("move")));
        });
        todoListReadModelDTO.getDeferredTodos().forEach(todoDTO -> {
            todoDTO.add(hateoasLinkGenerator.deleteTodoLink(listId, todoDTO.getIdentifier()).withRel("delete"));
            todoDTO.add(hateoasLinkGenerator.updateTodoLink(listId, todoDTO.getIdentifier()).withRel("update"));
            todoDTO.add(hateoasLinkGenerator.completeTodoLink(listId, todoDTO.getIdentifier()).withRel("complete"));

            todoListReadModelDTO.getDeferredTodos().forEach(targetTodoDTO ->
                todoDTO.add(hateoasLinkGenerator.moveTodoLink(
                    listId,
                    todoDTO.getIdentifier(), targetTodoDTO.getIdentifier()).withRel("move")));
        });
        TodoListReadModelResponse todoListReadModelResponse = new TodoListReadModelResponse(todoListReadModelDTO);
        todoListReadModelResponse.add(hateoasLinkGenerator.listLink(listId).withSelfRel());
        return todoListReadModelResponse;
    }
}