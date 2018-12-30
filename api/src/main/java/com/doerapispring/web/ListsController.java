package com.doerapispring.web;

import com.doerapispring.authentication.AuthenticatedUser;
import com.doerapispring.domain.ListApplicationService;
import com.doerapispring.domain.ListId;
import com.doerapispring.domain.ReadOnlyTodoList;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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
        try {
            listApplicationService.unlock(authenticatedUser.getUser(), new ListId(listId));
            ResourcesResponse resourcesResponse = new ResourcesResponse();
            resourcesResponse.add(
                hateoasLinkGenerator.listUnlockLink(listId).withSelfRel(),
                hateoasLinkGenerator.listLink(listId).withRel("list"));
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(resourcesResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping("/lists/{listId}")
    @ResponseBody
    ResponseEntity<TodoListResponse> show(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                          @PathVariable String listId) {
        try {
            ReadOnlyTodoList readOnlyTodoList = listApplicationService.get(authenticatedUser.getUser(), new ListId(listId));
            TodoListDTO todoListDTO = new TodoListDTO(
                readOnlyTodoList.getSectionName(),
                readOnlyTodoList.getDeferredSectionName(),
                readOnlyTodoList.getTodos().stream()
                    .map(todo -> new TodoDTO(
                        todo.getTodoId().getIdentifier(),
                        todo.getTask()))
                    .collect(toList()),
                readOnlyTodoList.getDeferredTodos().stream()
                    .map(todo -> new TodoDTO(
                        todo.getTodoId().getIdentifier(),
                        todo.getTask()))
                    .collect(toList()),
                readOnlyTodoList.unlockDuration()
            );
            todoListDTO.add(hateoasLinkGenerator.createDeferredTodoLink(listId).withRel("createDeferred"));
            if (readOnlyTodoList.isAbleToBeUnlocked()) {
                todoListDTO.add(hateoasLinkGenerator.listUnlockLink(listId).withRel("unlock"));
            }
            if (readOnlyTodoList.isFull()) {
                todoListDTO.add(hateoasLinkGenerator.displaceTodoLink(listId).withRel("displace"));
            } else {
                todoListDTO.add(hateoasLinkGenerator.createTodoLink(listId).withRel("create"));
            }
            if (readOnlyTodoList.isAbleToBeReplenished()) {
                todoListDTO.add(hateoasLinkGenerator.listPullTodosLink(listId).withRel("pull"));
            }
            if (readOnlyTodoList.isAbleToBeEscalated()) {
                todoListDTO.add(hateoasLinkGenerator.listEscalateTodoLink(listId).withRel("escalate"));
            }
            todoListDTO.getTodos().forEach(todoDTO -> {
                todoDTO.add(hateoasLinkGenerator.deleteTodoLink(listId, todoDTO.getIdentifier()).withRel("delete"));
                todoDTO.add(hateoasLinkGenerator.updateTodoLink(listId, todoDTO.getIdentifier()).withRel("update"));
                todoDTO.add(hateoasLinkGenerator.completeTodoLink(listId, todoDTO.getIdentifier()).withRel("complete"));

                todoListDTO.getTodos().forEach(targetTodoDTO ->
                    todoDTO.add(hateoasLinkGenerator.moveTodoLink(
                        listId,
                        todoDTO.getIdentifier(), targetTodoDTO.getIdentifier()).withRel("move")));
            });
            todoListDTO.getDeferredTodos().forEach(todoDTO -> {
                todoDTO.add(hateoasLinkGenerator.deleteTodoLink(listId, todoDTO.getIdentifier()).withRel("delete"));
                todoDTO.add(hateoasLinkGenerator.updateTodoLink(listId, todoDTO.getIdentifier()).withRel("update"));
                todoDTO.add(hateoasLinkGenerator.completeTodoLink(listId, todoDTO.getIdentifier()).withRel("complete"));

                todoListDTO.getDeferredTodos().forEach(targetTodoDTO ->
                    todoDTO.add(hateoasLinkGenerator.moveTodoLink(
                        listId,
                        todoDTO.getIdentifier(), targetTodoDTO.getIdentifier()).withRel("move")));
            });
            TodoListResponse todoListResponse = new TodoListResponse(todoListDTO);
            todoListResponse.add(hateoasLinkGenerator.listLink(listId).withSelfRel());
            return ResponseEntity.ok(todoListResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping(value = "/lists/{listId}/completed")
    @ResponseBody
    ResponseEntity<CompletedListResponse> showCompleted(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                                        @PathVariable String listId) {
        try {
            CompletedListDTO completedListDTO = new CompletedListDTO(
                listApplicationService.getCompleted(authenticatedUser.getUser(), new ListId(listId)).stream()
                    .map(completedTodo -> new CompletedTodoDTO(completedTodo.getTask(), completedTodo.getCompletedAt()))
                    .collect(toList()));
            CompletedListResponse completedListResponse = new CompletedListResponse(completedListDTO);
            completedListResponse.add(hateoasLinkGenerator.completedListLink(listId).withSelfRel());
            return ResponseEntity.ok(completedListResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
