package com.doerapispring.web;

import com.doerapispring.authentication.AuthenticatedUser;
import com.doerapispring.domain.ListApplicationService;
import com.doerapispring.domain.ListId;
import com.doerapispring.domain.TodoListReadModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static java.util.stream.Collectors.toList;

@RestController
@CrossOrigin
@RequestMapping(value = "/v1")
class ListsController {
    private final HateoasLinkGenerator hateoasLinkGenerator;
    private final ListApplicationService listApplicationService;

    ListsController(HateoasLinkGenerator hateoasLinkGenerator, ListApplicationService listApplicationService) {
        this.hateoasLinkGenerator = hateoasLinkGenerator;
        this.listApplicationService = listApplicationService;
    }

    @PostMapping(value = "/lists/{listId}/unlock")
    @ResponseBody
    ResponseEntity<ResourcesResponse> unlock(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                             @PathVariable String listId) {
        listApplicationService.unlock(authenticatedUser.getUser(), new ListId(listId));
        ResourcesResponse resourcesResponse = new ResourcesResponse();
        resourcesResponse.add(
            hateoasLinkGenerator.listUnlockLink(listId).withSelfRel(),
            hateoasLinkGenerator.listLink(listId).withRel("list"));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(resourcesResponse);
    }

    @RequestMapping("/lists/{listId}")
    @ResponseBody
    ResponseEntity<TodoListReadModelResponse> show(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                                   @PathVariable String listId) {
        TodoListReadModel todoListReadModel = listApplicationService.get(authenticatedUser.getUser(), new ListId(listId));
        TodoListReadModelDTO todoListReadModelDTO = new TodoListReadModelDTO(
            todoListReadModel.getProfileName(),
            todoListReadModel.getSectionName(),
            todoListReadModel.getDeferredSectionName(),
            todoListReadModel.getTodos().stream()
                .map(todo -> new TodoDTO(
                    todo.getTodoId().getIdentifier(),
                    todo.getTask()))
                .collect(toList()),
            todoListReadModel.getDeferredTodos().stream()
                .map(todo -> new TodoDTO(
                    todo.getTodoId().getIdentifier(),
                    todo.getTask()))
                .collect(toList()),
            todoListReadModel.unlockDuration()
        );
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
        return ResponseEntity.ok(todoListReadModelResponse);
    }

    @GetMapping(value = "/lists/{listId}/completed")
    @ResponseBody
    ResponseEntity<CompletedListResponse> showCompleted(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                                        @PathVariable String listId) {
        CompletedListDTO completedListDTO = new CompletedListDTO(
            listApplicationService.getCompleted(authenticatedUser.getUser(), new ListId(listId)).stream()
                .map(completedTodo -> new CompletedTodoDTO(completedTodo.getTask(), completedTodo.getCompletedAt()))
                .collect(toList()));
        CompletedListResponse completedListResponse = new CompletedListResponse(completedListDTO);
        completedListResponse.add(hateoasLinkGenerator.completedListLink(listId).withSelfRel());
        return ResponseEntity.ok(completedListResponse);
    }

    @GetMapping(value = "/lists")
    @ResponseBody
    ResponseEntity<TodoListResponse> showAll(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        List<TodoListDTO> todoListDTOS = listApplicationService.getAll(authenticatedUser.getUser()).stream()
            .map(todoList -> {
                TodoListDTO todoListDTO = new TodoListDTO(todoList.getName());
                todoListDTO.add(hateoasLinkGenerator.listLink(todoList.getListId().get()).withRel("list"));
                return todoListDTO;
            })
            .collect(toList());
        TodoListResponse todoListResponse = new TodoListResponse(todoListDTOS);
        todoListResponse.add(hateoasLinkGenerator.showListsLink());
        return ResponseEntity.ok(todoListResponse);
    }

    @PostMapping(value = "/lists")
    @ResponseBody
    ResponseEntity<ResourcesResponse> create(
        @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
        @RequestBody ListForm listForm) {
        listApplicationService.create(authenticatedUser.getUser(), listForm.getName());
        ResourcesResponse resourcesResponse = new ResourcesResponse();
        resourcesResponse.add(hateoasLinkGenerator.createListsLink().withSelfRel());
        return ResponseEntity.status(HttpStatus.CREATED).body(resourcesResponse);
    }
}
