package com.doerapispring.web;

import com.doerapispring.authentication.AuthenticatedUser;
import com.doerapispring.domain.ListId;
import com.doerapispring.domain.TodoApplicationService;
import com.doerapispring.domain.TodoId;
import com.doerapispring.domain.TodoListModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import scala.Function0;

@RestController
@CrossOrigin
@RequestMapping(value = "/v1")
class TodosController {
    private final HateoasLinkGenerator hateoasLinkGenerator;
    private final TodoApplicationService todoApplicationService;

    TodosController(HateoasLinkGenerator hateoasLinkGenerator,
                    TodoApplicationService todoApplicationService) {
        this.hateoasLinkGenerator = hateoasLinkGenerator;
        this.todoApplicationService = todoApplicationService;
    }

    @RequestMapping(value = "/lists/{listId}/todos/{todoId}", method = RequestMethod.DELETE)
    @ResponseBody
    ResponseEntity<ResourcesResponse> delete(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                             @PathVariable String listId,
                                             @PathVariable String todoId) {
        todoApplicationService.performOperation(
                authenticatedUser.getUser(),
                new ListId(listId),
                todoList -> TodoListModel.delete(todoList, new TodoId(todoId)));
        ResourcesResponse resourcesResponse = new ResourcesResponse();
        resourcesResponse.add(hateoasLinkGenerator.deleteTodoLink(listId, todoId).withSelfRel(),
                hateoasLinkGenerator.listLink(listId).withRel("list"));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(resourcesResponse);
    }

    @RequestMapping(value = "/lists/{listId}/displace", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<ResourcesResponse> displace(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                               @PathVariable String listId,
                                               @RequestBody TodoForm todoForm) {
        todoApplicationService.performOperation(
                authenticatedUser.getUser(),
                new ListId(listId),
                (todoList, todoId) ->
                        TodoListModel.displaceCapability(todoList)
                                .flatMap(capability -> capability.apply(todoId, todoForm.getTask())));
        ResourcesResponse resourcesResponse = new ResourcesResponse();
        resourcesResponse.add(hateoasLinkGenerator.displaceTodoLink(listId).withSelfRel(),
            hateoasLinkGenerator.listLink(listId).withRel("list"));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(resourcesResponse);
    }

    @RequestMapping(value = "/lists/{listId}/todos/{todoId}", method = RequestMethod.PUT)
    @ResponseBody
    ResponseEntity<ResourcesResponse> update(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                             @PathVariable String listId,
                                             @PathVariable String todoId,
                                             @RequestBody TodoForm todoForm) {
        todoApplicationService.performOperation(
                authenticatedUser.getUser(),
                new ListId(listId),
                todoList -> TodoListModel.update(todoList, new TodoId(todoId), todoForm.getTask()));
        ResourcesResponse resourcesResponse = new ResourcesResponse();
        resourcesResponse.add(hateoasLinkGenerator.updateTodoLink(listId, todoId).withSelfRel(),
            hateoasLinkGenerator.listLink(listId).withRel("list"));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(resourcesResponse);
    }

    @RequestMapping(value = "/lists/{listId}/todos/{todoId}/complete", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<ResourcesResponse> complete(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                               @PathVariable String listId,
                                               @PathVariable String todoId) {
        todoApplicationService.performOperation(
                authenticatedUser.getUser(),
                new ListId(listId),
                todoList -> TodoListModel.complete(todoList, new TodoId(todoId)));
        ResourcesResponse resourcesResponse = new ResourcesResponse();
        resourcesResponse.add(
            hateoasLinkGenerator.completeTodoLink(listId, todoId).withSelfRel(),
            hateoasLinkGenerator.listLink(listId).withRel("list"));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(resourcesResponse);
    }

    @RequestMapping(value = "/lists/{listId}/todos/{todoId}/move/{targetTodoId}", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<ResourcesResponse> move(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                           @PathVariable String listId,
                                           @PathVariable String todoId,
                                           @PathVariable String targetTodoId) {
        todoApplicationService.performOperation(
                authenticatedUser.getUser(),
                new ListId(listId),
                todoList -> TodoListModel.move(todoList, new TodoId(todoId), new TodoId(targetTodoId)));
        ResourcesResponse resourcesResponse = new ResourcesResponse();
        resourcesResponse.add(
            hateoasLinkGenerator.moveTodoLink(listId, todoId, targetTodoId).withSelfRel(),
            hateoasLinkGenerator.listLink(listId).withRel("list"));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(resourcesResponse);
    }

    @RequestMapping(value = "/lists/{listId}/pull", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<ResourcesResponse> pull(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                           @PathVariable String listId) {
        todoApplicationService.performOperation(
                authenticatedUser.getUser(),
                new ListId(listId),
                (todoList, todoId) ->
                        TodoListModel.pullCapability(todoList)
                                .flatMap(Function0::apply));
        ResourcesResponse resourcesResponse = new ResourcesResponse();
        resourcesResponse.add(
            hateoasLinkGenerator.listPullTodosLink(listId).withSelfRel(),
            hateoasLinkGenerator.listLink(listId).withRel("list"));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(resourcesResponse);
    }

    @RequestMapping(value = "/lists/{listId}/escalate", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<ResourcesResponse> escalate(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                               @PathVariable String listId) {
        todoApplicationService.performOperation(
                authenticatedUser.getUser(),
                new ListId(listId),
                (todoList, todoId) ->
                        TodoListModel.escalateCapability(todoList)
                                .flatMap(Function0::apply));
        ResourcesResponse resourcesResponse = new ResourcesResponse();
        resourcesResponse.add(hateoasLinkGenerator.listEscalateTodoLink(listId).withSelfRel());
        resourcesResponse.add(hateoasLinkGenerator.listLink(listId).withRel("list"));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(resourcesResponse);
    }

    @RequestMapping(value = "/lists/{listId}/todos", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<ResourcesResponse> create(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                             @PathVariable String listId,
                                             @RequestBody TodoForm todoForm
    ) {
        todoApplicationService.performOperation(
                authenticatedUser.getUser(),
                new ListId(listId),
                (todoList, todoId) -> TodoListModel.add(todoList, todoId, todoForm.getTask()));
        ResourcesResponse resourcesResponse = new ResourcesResponse();
        resourcesResponse.add(
            hateoasLinkGenerator.createTodoLink(listId).withSelfRel(),
            hateoasLinkGenerator.listLink(listId).withRel("list"));
        return ResponseEntity.status(HttpStatus.CREATED).body(resourcesResponse);
    }

    @RequestMapping(value = "/lists/{listId}/deferredTodos", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<ResourcesResponse> createDeferred(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                                     @PathVariable String listId,
                                                     @RequestBody TodoForm todoForm) {
        todoApplicationService.performOperation(
                authenticatedUser.getUser(),
                new ListId(listId),
                (todoList, todoId) -> TodoListModel.addDeferred(todoList, todoId, todoForm.getTask()));
        ResourcesResponse resourcesResponse = new ResourcesResponse();
        resourcesResponse.add(
            hateoasLinkGenerator.createDeferredTodoLink(listId).withSelfRel(),
            hateoasLinkGenerator.listLink(listId).withRel("list"));
        return ResponseEntity.status(HttpStatus.CREATED).body(resourcesResponse);
    }
}
