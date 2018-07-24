package com.doerapispring.web;

import com.doerapispring.authentication.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
@RequestMapping(value = "/v1")
class TodosController {
    private final HateoasLinkGenerator hateoasLinkGenerator;
    private final TodoApiService todoApiService;

    @Autowired
    TodosController(HateoasLinkGenerator hateoasLinkGenerator, TodoApiService todoApiService) {
        this.hateoasLinkGenerator = hateoasLinkGenerator;
        this.todoApiService = todoApiService;
    }

    @RequestMapping(value = "/todos/{localId}", method = RequestMethod.DELETE)
    @ResponseBody
    ResponseEntity<ResourcesResponse> delete(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                             @PathVariable(value = "localId") String localId) {
        try {
            todoApiService.delete(authenticatedUser, localId);
            ResourcesResponse resourcesResponse = new ResourcesResponse();
            resourcesResponse.add(hateoasLinkGenerator.deleteTodoLink(localId).withSelfRel(),
                hateoasLinkGenerator.listLink().withRel("list"));
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(resourcesResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @RequestMapping(value = "/list/displace", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<ResourcesResponse> displace(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                               @RequestBody TodoForm todoForm) throws InvalidRequestException {
        todoApiService.displace(authenticatedUser, todoForm.getTask());
        ResourcesResponse resourcesResponse = new ResourcesResponse();
        resourcesResponse.add(hateoasLinkGenerator.displaceTodoLink().withSelfRel(),
            hateoasLinkGenerator.listLink().withRel("list"));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(resourcesResponse);
    }

    @RequestMapping(value = "/todos/{localId}", method = RequestMethod.PUT)
    @ResponseBody
    ResponseEntity<ResourcesResponse> update(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                             @PathVariable(value = "localId") String localId,
                                             @RequestBody TodoForm todoForm) throws InvalidRequestException {
        todoApiService.update(authenticatedUser, localId, todoForm.getTask());
        ResourcesResponse resourcesResponse = new ResourcesResponse();
        resourcesResponse.add(hateoasLinkGenerator.updateTodoLink(localId).withSelfRel(),
            hateoasLinkGenerator.listLink().withRel("list"));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(resourcesResponse);
    }

    @RequestMapping(value = "/todos/{localId}/complete", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<ResourcesResponse> complete(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                               @PathVariable(value = "localId") String localId) {
        try {
            todoApiService.complete(authenticatedUser, localId);
            ResourcesResponse resourcesResponse = new ResourcesResponse();
            resourcesResponse.add(hateoasLinkGenerator.completeTodoLink(localId).withSelfRel(),
                hateoasLinkGenerator.listLink().withRel("list"));
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(resourcesResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @RequestMapping(value = "/todos/{localId}/move/{targetLocalId}", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<ResourcesResponse> move(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                           @PathVariable String localId,
                                           @PathVariable String targetLocalId) {
        try {
            todoApiService.move(authenticatedUser, localId, targetLocalId);
            ResourcesResponse resourcesResponse = new ResourcesResponse();
            resourcesResponse.add(
                hateoasLinkGenerator.moveTodoLink(localId, targetLocalId).withSelfRel(),
                hateoasLinkGenerator.listLink().withRel("list"));
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(resourcesResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @RequestMapping(value = "/completedTodos", method = RequestMethod.GET)
    @ResponseBody
    ResponseEntity<CompletedTodosResponse> completedTodos(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        try {
            CompletedTodoListDTO completedTodoListDTO = todoApiService.getCompleted(authenticatedUser);
            CompletedTodosResponse todosResponse = new CompletedTodosResponse(completedTodoListDTO.getCompletedTodoDTOs());
            todosResponse.add(hateoasLinkGenerator.completedTodosLink().withSelfRel());
            todosResponse.add(hateoasLinkGenerator.listLink().withRel("list"));
            return ResponseEntity.status(HttpStatus.OK).body(todosResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @RequestMapping(value = "/list/pull", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<ResourcesResponse> pull(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        try {
            todoApiService.pull(authenticatedUser);
            ResourcesResponse resourcesResponse = new ResourcesResponse();
            resourcesResponse.add(
                hateoasLinkGenerator.listPullTodosLink().withSelfRel(),
                hateoasLinkGenerator.listLink().withRel("list"));
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(resourcesResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @RequestMapping(value = "/list/todos", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<ResourcesResponse> create(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                             @RequestBody TodoForm todoForm) throws InvalidRequestException {
        todoApiService.create(authenticatedUser, todoForm.getTask());
        ResourcesResponse resourcesResponse = new ResourcesResponse();
        resourcesResponse.add(
            hateoasLinkGenerator.createTodoLink().withSelfRel(),
            hateoasLinkGenerator.listLink().withRel("list"));
        return ResponseEntity.status(HttpStatus.CREATED).body(resourcesResponse);
    }

    @RequestMapping(value = "/list/todos", method = RequestMethod.GET)
    @ResponseBody
    ResponseEntity<TodosResponse> todos(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        try {
            TodoListDTO todoListDTO = todoApiService.getTodos(authenticatedUser);
            todoListDTO.getTodoDTOs().forEach(todoDTO -> {
                todoDTO.add(hateoasLinkGenerator.deleteTodoLink(todoDTO.getLocalIdentifier()).withRel("delete"));
                todoDTO.add(hateoasLinkGenerator.updateTodoLink(todoDTO.getLocalIdentifier()).withRel("update"));
                todoDTO.add(hateoasLinkGenerator.completeTodoLink(todoDTO.getLocalIdentifier()).withRel("complete"));

                todoListDTO.getTodoDTOs().forEach(targetTodoDTO ->
                    todoDTO.add(hateoasLinkGenerator.moveTodoLink(
                        todoDTO.getLocalIdentifier(),
                        targetTodoDTO.getLocalIdentifier()).withRel("move")));
            });
            TodosResponse todosResponse = new TodosResponse(todoListDTO.getTodoDTOs());
            todosResponse.add(hateoasLinkGenerator.todosLink().withSelfRel());
            todosResponse.add(hateoasLinkGenerator.listLink().withRel("list"));
            return ResponseEntity.status(HttpStatus.OK).body(todosResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @RequestMapping(value = "/list/deferredTodos", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<ResourcesResponse> createDeferred(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                                     @RequestBody TodoForm todoForm) throws InvalidRequestException {
        todoApiService.createDeferred(authenticatedUser, todoForm.getTask());
        ResourcesResponse resourcesResponse = new ResourcesResponse();
        resourcesResponse.add(
            hateoasLinkGenerator.createDeferredTodoLink().withSelfRel(),
            hateoasLinkGenerator.listLink().withRel("list"));
        return ResponseEntity.status(HttpStatus.CREATED).body(resourcesResponse);
    }

    @RequestMapping(value = "/list/deferredTodos", method = RequestMethod.GET)
    @ResponseBody
    ResponseEntity<TodosResponse> deferredTodos(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        try {
            TodoListDTO todoListDTO = todoApiService.getDeferredTodos(authenticatedUser);
            todoListDTO.getTodoDTOs().forEach(todoDTO -> {
                todoDTO.add(hateoasLinkGenerator.deleteTodoLink(todoDTO.getLocalIdentifier()).withRel("delete"));
                todoDTO.add(hateoasLinkGenerator.updateTodoLink(todoDTO.getLocalIdentifier()).withRel("update"));
                todoDTO.add(hateoasLinkGenerator.completeTodoLink(todoDTO.getLocalIdentifier()).withRel("complete"));

                todoListDTO.getTodoDTOs().forEach(targetTodoDTO ->
                    todoDTO.add(hateoasLinkGenerator.moveTodoLink(
                        todoDTO.getLocalIdentifier(),
                        targetTodoDTO.getLocalIdentifier()).withRel("move")));
            });
            TodosResponse todosResponse = new TodosResponse(todoListDTO.getTodoDTOs());
            todosResponse.add(hateoasLinkGenerator.deferredTodosLink().withSelfRel());
            todosResponse.add(hateoasLinkGenerator.listLink().withRel("list"));
            return ResponseEntity.status(HttpStatus.OK).body(todosResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
