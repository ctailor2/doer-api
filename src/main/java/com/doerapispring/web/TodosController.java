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
    ResponseEntity<TodosResponse> index(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        try {
            TodoListDTO todoListDTO = todoApiService.get(authenticatedUser);
            TodosResponse todosResponse = new TodosResponse(todoListDTO.getTodoDTOs());
            todosResponse.add(hateoasLinkGenerator.todosLink().withSelfRel(),
                    hateoasLinkGenerator.createTodoForLaterLink().withRel("todoLater"));
            boolean canScheduleForNow = todoListDTO.isSchedulingForNowAllowed();
            if (canScheduleForNow) {
                todosResponse.add(hateoasLinkGenerator.createTodoForNowLink().withRel("todoNow"));
            }
            todoListDTO.getTodoDTOs().stream().forEach(todoDTO -> {
                todoDTO.add(hateoasLinkGenerator.deleteTodoLink(todoDTO.getLocalIdentifier()).withRel("delete"));
                todoDTO.add(hateoasLinkGenerator.updateTodoLink(todoDTO.getLocalIdentifier()).withRel("update"));
                todoDTO.add(hateoasLinkGenerator.completeTodoLink(todoDTO.getLocalIdentifier()).withRel("complete"));
                if (!canScheduleForNow) {
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
    ResponseEntity<TodoLinksResponse> createForNow(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                                   @RequestBody TodoForm todoForm) {
        try {
            todoApiService.create(authenticatedUser, todoForm.getTask(), "now");
            TodoLinksResponse todoLinksResponse = new TodoLinksResponse();
            todoLinksResponse.add(hateoasLinkGenerator.createTodoForNowLink().withSelfRel(),
                    hateoasLinkGenerator.todosLink().withRel("todos"));
            return ResponseEntity.status(HttpStatus.CREATED).body(todoLinksResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @RequestMapping(value = "/todoLater", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<TodoLinksResponse> createForLater(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                                     @RequestBody TodoForm todoForm) {
        try {
            todoApiService.create(authenticatedUser, todoForm.getTask(), "later");
            TodoLinksResponse todoLinksResponse = new TodoLinksResponse();
            todoLinksResponse.add(hateoasLinkGenerator.createTodoForLaterLink().withSelfRel(),
                    hateoasLinkGenerator.todosLink().withRel("todos"));
            return ResponseEntity.status(HttpStatus.CREATED).body(todoLinksResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @RequestMapping(value = "/todos/{localId}", method = RequestMethod.DELETE)
    @ResponseBody
    ResponseEntity<TodoLinksResponse> delete(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                             @PathVariable(value = "localId") String localId) {
        try {
            todoApiService.delete(authenticatedUser, localId);
            TodoLinksResponse todoLinksResponse = new TodoLinksResponse();
            todoLinksResponse.add(hateoasLinkGenerator.deleteTodoLink(localId).withSelfRel(),
                    hateoasLinkGenerator.todosLink().withRel("todos"));
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(todoLinksResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @RequestMapping(value = "/todos/{localId}/displace", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<TodoLinksResponse> displace(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                               @PathVariable(value = "localId") String localId,
                                               @RequestBody TodoForm todoForm) {
        try {
            todoApiService.displace(authenticatedUser, localId, todoForm.getTask());
            TodoLinksResponse todoLinksResponse = new TodoLinksResponse();
            todoLinksResponse.add(hateoasLinkGenerator.displaceTodoLink(localId).withSelfRel(),
                    hateoasLinkGenerator.todosLink().withRel("todos"));
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(todoLinksResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @RequestMapping(value = "/todos/{localId}", method = RequestMethod.PUT)
    @ResponseBody
    ResponseEntity<TodoLinksResponse> update(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                             @PathVariable(value = "localId") String localId,
                                             @RequestBody TodoForm todoForm) {
        try {
            todoApiService.update(authenticatedUser, localId, todoForm.getTask());
            TodoLinksResponse todoLinksResponse = new TodoLinksResponse();
            todoLinksResponse.add(hateoasLinkGenerator.updateTodoLink(localId).withSelfRel(),
                    hateoasLinkGenerator.todosLink().withRel("todos"));
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(todoLinksResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @RequestMapping(value = "/todos/{localId}/complete", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<TodoLinksResponse> complete(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                             @PathVariable(value = "localId") String localId) {
        try {
            todoApiService.complete(authenticatedUser, localId);
            TodoLinksResponse todoLinksResponse = new TodoLinksResponse();
            todoLinksResponse.add(hateoasLinkGenerator.completeTodoLink(localId).withSelfRel(),
                    hateoasLinkGenerator.todosLink().withRel("todos"));
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(todoLinksResponse);
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
            todosResponse.add(hateoasLinkGenerator.todosLink().withRel("todos"));
            return ResponseEntity.status(HttpStatus.OK).body(todosResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
