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
    private TodoApiService todoApiService;

    @Autowired
    TodosController(HateoasLinkGenerator hateoasLinkGenerator, TodoApiService todoApiService) {
        this.hateoasLinkGenerator = hateoasLinkGenerator;
        this.todoApiService = todoApiService;
    }

    @RequestMapping(value = "/todos", method = RequestMethod.GET)
    @ResponseBody
    ResponseEntity<TodosResponse> index(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                        @RequestParam(defaultValue = "now") String scheduling) {
        try {
            TodoListDTO todoListDTO = todoApiService.getSubList(authenticatedUser, scheduling);
            TodosResponse todosResponse = new TodosResponse(todoListDTO.getTodoDTOs());
            todosResponse.add(hateoasLinkGenerator.todosLink(scheduling).withSelfRel());
            todoListDTO.getTodoDTOs().stream().forEach(todoDTO -> {
                todoDTO.add(hateoasLinkGenerator.deleteTodoLink(todoDTO.getLocalIdentifier()).withRel("delete"));
                todoDTO.add(hateoasLinkGenerator.updateTodoLink(todoDTO.getLocalIdentifier()).withRel("update"));
                todoDTO.add(hateoasLinkGenerator.completeTodoLink(todoDTO.getLocalIdentifier()).withRel("complete"));

                todoListDTO.getTodoDTOs().stream().forEach(targetTodoDTO ->
                        todoDTO.add(hateoasLinkGenerator.moveTodoLink(
                                todoDTO.getLocalIdentifier(),
                                targetTodoDTO.getLocalIdentifier()).withRel("move")));

                if (todoListDTO.isDisplacementAllowed()) {
                    todoDTO.add(hateoasLinkGenerator.displaceTodoLink(todoDTO.getLocalIdentifier()).withRel("displace"));
                }
            });
            return ResponseEntity.status(HttpStatus.OK)
                    .body(todosResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @RequestMapping(value = "/todoNow", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<ResourcesResponse> createForNow(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                                   @RequestBody TodoForm todoForm) {
        try {
            todoApiService.create(authenticatedUser, todoForm.getTask(), "now");
            ResourcesResponse resourcesResponse = new ResourcesResponse();
            resourcesResponse.add(hateoasLinkGenerator.createTodoForNowLink().withSelfRel(),
                    hateoasLinkGenerator.todosLink("now").withRel("nowTodos"),
                    hateoasLinkGenerator.todosLink("later").withRel("laterTodos"),
                    hateoasLinkGenerator.todoResourcesLink().withRel("todoResources"));
            return ResponseEntity.status(HttpStatus.CREATED).body(resourcesResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @RequestMapping(value = "/todoLater", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<ResourcesResponse> createForLater(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                                     @RequestBody TodoForm todoForm) {
        try {
            todoApiService.create(authenticatedUser, todoForm.getTask(), "later");
            ResourcesResponse resourcesResponse = new ResourcesResponse();
            resourcesResponse.add(hateoasLinkGenerator.createTodoForLaterLink().withSelfRel(),
                    hateoasLinkGenerator.todosLink("now").withRel("nowTodos"),
                    hateoasLinkGenerator.todosLink("later").withRel("laterTodos"),
                    hateoasLinkGenerator.todoResourcesLink().withRel("todoResources"));
            return ResponseEntity.status(HttpStatus.CREATED).body(resourcesResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @RequestMapping(value = "/todos/{localId}", method = RequestMethod.DELETE)
    @ResponseBody
    ResponseEntity<ResourcesResponse> delete(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                             @PathVariable(value = "localId") String localId) {
        try {
            todoApiService.delete(authenticatedUser, localId);
            ResourcesResponse resourcesResponse = new ResourcesResponse();
            resourcesResponse.add(hateoasLinkGenerator.deleteTodoLink(localId).withSelfRel(),
                    hateoasLinkGenerator.todosLink("now").withRel("nowTodos"),
                    hateoasLinkGenerator.todosLink("later").withRel("laterTodos"),
                    hateoasLinkGenerator.todoResourcesLink().withRel("todoResources"));
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(resourcesResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @RequestMapping(value = "/todos/{localId}/displace", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<ResourcesResponse> displace(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                               @PathVariable(value = "localId") String localId,
                                               @RequestBody TodoForm todoForm) {
        try {
            todoApiService.displace(authenticatedUser, localId, todoForm.getTask());
            ResourcesResponse resourcesResponse = new ResourcesResponse();
            resourcesResponse.add(hateoasLinkGenerator.displaceTodoLink(localId).withSelfRel(),
                    hateoasLinkGenerator.todosLink("now").withRel("nowTodos"),
                    hateoasLinkGenerator.todosLink("later").withRel("laterTodos"),
                    hateoasLinkGenerator.todoResourcesLink().withRel("todoResources"));
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(resourcesResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @RequestMapping(value = "/todos/{localId}", method = RequestMethod.PUT)
    @ResponseBody
    ResponseEntity<ResourcesResponse> update(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                             @PathVariable(value = "localId") String localId,
                                             @RequestBody TodoForm todoForm) {
        try {
            todoApiService.update(authenticatedUser, localId, todoForm.getTask());
            ResourcesResponse resourcesResponse = new ResourcesResponse();
            resourcesResponse.add(hateoasLinkGenerator.updateTodoLink(localId).withSelfRel(),
                    hateoasLinkGenerator.todosLink("now").withRel("nowTodos"),
                    hateoasLinkGenerator.todosLink("later").withRel("laterTodos"),
                    hateoasLinkGenerator.todoResourcesLink().withRel("todoResources"));
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(resourcesResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @RequestMapping(value = "/todos/{localId}/complete", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<ResourcesResponse> complete(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                               @PathVariable(value = "localId") String localId) {
        try {
            todoApiService.complete(authenticatedUser, localId);
            ResourcesResponse resourcesResponse = new ResourcesResponse();
            resourcesResponse.add(hateoasLinkGenerator.completeTodoLink(localId).withSelfRel(),
                    hateoasLinkGenerator.todosLink("now").withRel("nowTodos"),
                    hateoasLinkGenerator.todosLink("later").withRel("laterTodos"),
                    hateoasLinkGenerator.todoResourcesLink().withRel("todoResources"));
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(resourcesResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @RequestMapping(value = "/completedTodos", method = RequestMethod.GET)
    @ResponseBody
    ResponseEntity<TodosResponse> completedTodos(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        try {
            CompletedTodoListDTO completedTodoListDTO = todoApiService.getCompleted(authenticatedUser);
            TodosResponse todosResponse = new TodosResponse(completedTodoListDTO.getTodoDTOs());
            todosResponse.add(hateoasLinkGenerator.completedTodosLink().withSelfRel());
            return ResponseEntity.status(HttpStatus.OK).body(todosResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @RequestMapping(value = "/todos/{localId}/move/{targetLocalId}", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<ResourcesResponse> move(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                           @PathVariable(value = "localId") String localId,
                                           @PathVariable(value = "targetLocalId") String targetLocalId) {
        try {
            todoApiService.move(authenticatedUser, localId, targetLocalId);
            ResourcesResponse resourcesResponse = new ResourcesResponse();
            resourcesResponse.add(hateoasLinkGenerator.moveTodoLink(localId, targetLocalId).withSelfRel(),
                    hateoasLinkGenerator.todosLink("now").withRel("nowTodos"),
                    hateoasLinkGenerator.todosLink("later").withRel("laterTodos"),
                    hateoasLinkGenerator.todoResourcesLink().withRel("todoResources"));
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(resourcesResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @RequestMapping(value = "/todos/pull", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<ResourcesResponse> pull(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        try {
            todoApiService.pull(authenticatedUser);
            ResourcesResponse resourcesResponse = new ResourcesResponse();
            resourcesResponse.add(hateoasLinkGenerator.pullTodosLink().withSelfRel(),
                    hateoasLinkGenerator.todosLink("now").withRel("nowTodos"),
                    hateoasLinkGenerator.todosLink("later").withRel("laterTodos"),
                    hateoasLinkGenerator.todoResourcesLink().withRel("todoResources"));
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(resourcesResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
