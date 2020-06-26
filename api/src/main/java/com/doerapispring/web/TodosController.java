package com.doerapispring.web;

import com.doerapispring.authentication.AuthenticatedUser;
import com.doerapispring.domain.ListId;
import com.doerapispring.domain.TodoApplicationService;
import com.doerapispring.domain.TodoListModel;
import com.doerapispring.domain.events.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.util.Date;

@RestController
@CrossOrigin
@RequestMapping(value = "/v1")
class TodosController {
    private final HateoasLinkGenerator hateoasLinkGenerator;
    private final TodoApplicationService todoApplicationService;
    private final TodoListModelResourceTransformer todoListModelResourceTransformer;
    private final Clock clock;

    TodosController(HateoasLinkGenerator hateoasLinkGenerator,
                    TodoApplicationService todoApplicationService,
                    TodoListModelResourceTransformer todoListModelResourceTransformer,
                    Clock clock) {
        this.hateoasLinkGenerator = hateoasLinkGenerator;
        this.todoApplicationService = todoApplicationService;
        this.todoListModelResourceTransformer = todoListModelResourceTransformer;
        this.clock = clock;
    }

    @RequestMapping(value = "/lists/{listId}/todos/{todoId}", method = RequestMethod.DELETE)
    @ResponseBody
    ResponseEntity<TodoListReadModelResponse> delete(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                             @PathVariable String listId,
                                             @PathVariable String todoId) {
        TodoListReadModelResponse todoListReadModelResponse = todoApplicationService.performOperation(
                authenticatedUser.getUser(),
                new ListId(listId),
                () -> new TodoDeletedEvent(todoId),
                TodoListModel::applyEvent)
                .map(todoList -> todoListModelResourceTransformer.transform(todoList, Date.from(clock.instant())))
                .get();
        todoListReadModelResponse.add(hateoasLinkGenerator.deleteTodoLink(listId, todoId).withSelfRel(),
                hateoasLinkGenerator.listLink(listId).withRel("list"));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(todoListReadModelResponse);
    }

    @RequestMapping(value = "/lists/{listId}/displace", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<TodoListReadModelResponse> displace(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                               @PathVariable String listId,
                                               @RequestBody TodoForm todoForm) {
        TodoListReadModelResponse todoListReadModelResponse = todoApplicationService.performOperation(
                authenticatedUser.getUser(),
                new ListId(listId),
                (todoId) -> new TodoDisplacedEvent(todoId.getIdentifier(), todoForm.getTask()),
                TodoListModel::applyEvent)
                .map(todoList -> todoListModelResourceTransformer.transform(todoList, Date.from(clock.instant())))
                .get();
        todoListReadModelResponse.add(hateoasLinkGenerator.displaceTodoLink(listId).withSelfRel(),
                hateoasLinkGenerator.listLink(listId).withRel("list"));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(todoListReadModelResponse);
    }

    @RequestMapping(value = "/lists/{listId}/todos/{todoId}", method = RequestMethod.PUT)
    @ResponseBody
    ResponseEntity<TodoListReadModelResponse> update(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                             @PathVariable String listId,
                                             @PathVariable String todoId,
                                             @RequestBody TodoForm todoForm) {
        TodoListReadModelResponse todoListReadModelResponse = todoApplicationService.performOperation(
                authenticatedUser.getUser(),
                new ListId(listId),
                () -> new TodoUpdatedEvent(todoId, todoForm.getTask()),
                TodoListModel::applyEvent)
                .map(todoList -> todoListModelResourceTransformer.transform(todoList, Date.from(clock.instant())))
                .get();
        todoListReadModelResponse.add(hateoasLinkGenerator.updateTodoLink(listId, todoId).withSelfRel(),
                hateoasLinkGenerator.listLink(listId).withRel("list"));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(todoListReadModelResponse);
    }

    @RequestMapping(value = "/lists/{listId}/todos/{todoId}/complete", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<TodoListReadModelResponse> complete(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                               @PathVariable String listId,
                                               @PathVariable String todoId) {
        TodoListReadModelResponse todoListReadModelResponse = todoApplicationService.performOperation(
                authenticatedUser.getUser(),
                new ListId(listId),
                () -> new TodoCompletedEvent(todoId),
                TodoListModel::applyEvent)
                .map(todoList -> todoListModelResourceTransformer.transform(todoList, Date.from(clock.instant())))
                .get();
        todoListReadModelResponse.add(
                hateoasLinkGenerator.completeTodoLink(listId, todoId).withSelfRel(),
                hateoasLinkGenerator.listLink(listId).withRel("list"));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(todoListReadModelResponse);
    }

    @RequestMapping(value = "/lists/{listId}/todos/{todoId}/move/{targetTodoId}", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<TodoListReadModelResponse> move(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                           @PathVariable String listId,
                                           @PathVariable String todoId,
                                           @PathVariable String targetTodoId) {
        TodoListReadModelResponse todoListReadModelResponse = todoApplicationService.performOperation(
                authenticatedUser.getUser(),
                new ListId(listId),
                () -> new TodoMovedEvent(todoId, targetTodoId),
                TodoListModel::applyEvent)
                .map(todoList -> todoListModelResourceTransformer.transform(todoList, Date.from(clock.instant())))
                .get();
        todoListReadModelResponse.add(
                hateoasLinkGenerator.moveTodoLink(listId, todoId, targetTodoId).withSelfRel(),
                hateoasLinkGenerator.listLink(listId).withRel("list"));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(todoListReadModelResponse);
    }

    @RequestMapping(value = "/lists/{listId}/pull", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<TodoListReadModelResponse> pull(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                           @PathVariable String listId) {
        TodoListReadModelResponse todoListReadModelResponse = todoApplicationService.performOperation(
                authenticatedUser.getUser(),
                new ListId(listId),
                PulledEvent::new,
                TodoListModel::applyEvent)
                .map(todoList -> todoListModelResourceTransformer.transform(todoList, Date.from(clock.instant())))
                .get();
        todoListReadModelResponse.add(
                hateoasLinkGenerator.listPullTodosLink(listId).withSelfRel(),
                hateoasLinkGenerator.listLink(listId).withRel("list"));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(todoListReadModelResponse);
    }

    @RequestMapping(value = "/lists/{listId}/escalate", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<TodoListReadModelResponse> escalate(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                               @PathVariable String listId) {
        TodoListReadModelResponse todoListReadModelResponse = todoApplicationService.performOperation(
                authenticatedUser.getUser(),
                new ListId(listId),
                EscalatedEvent::new,
                TodoListModel::applyEvent)
                .map(todoList -> todoListModelResourceTransformer.transform(todoList, Date.from(clock.instant())))
                .get();
        todoListReadModelResponse.add(hateoasLinkGenerator.listEscalateTodoLink(listId).withSelfRel());
        todoListReadModelResponse.add(hateoasLinkGenerator.listLink(listId).withRel("list"));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(todoListReadModelResponse);
    }

    @RequestMapping(value = "/lists/{listId}/todos", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<TodoListReadModelResponse> create(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                             @PathVariable String listId,
                                             @RequestBody TodoForm todoForm
    ) {
        TodoListReadModelResponse todoListReadModelResponse = todoApplicationService.performOperation(
                authenticatedUser.getUser(),
                new ListId(listId),
                todoId -> new TodoAddedEvent(todoId.getIdentifier(), todoForm.getTask()),
                TodoListModel::applyEvent)
                .map(todoList -> todoListModelResourceTransformer.transform(todoList, Date.from(clock.instant())))
                .get();
        todoListReadModelResponse.add(
                hateoasLinkGenerator.createTodoLink(listId).withSelfRel(),
                hateoasLinkGenerator.listLink(listId).withRel("list"));
        return ResponseEntity.status(HttpStatus.CREATED).body(todoListReadModelResponse);
    }

    @RequestMapping(value = "/lists/{listId}/deferredTodos", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<TodoListReadModelResponse> createDeferred(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                                     @PathVariable String listId,
                                                     @RequestBody TodoForm todoForm) {
        TodoListReadModelResponse todoListReadModelResponse = todoApplicationService.performOperation(
                authenticatedUser.getUser(),
                new ListId(listId),
                (todoId) -> new DeferredTodoAddedEvent(todoId.getIdentifier(), todoForm.getTask()),
                TodoListModel::applyEvent)
                .map(todoList -> todoListModelResourceTransformer.transform(todoList, Date.from(clock.instant())))
                .get();
        todoListReadModelResponse.add(
                hateoasLinkGenerator.createDeferredTodoLink(listId).withSelfRel(),
                hateoasLinkGenerator.listLink(listId).withRel("list"));
        return ResponseEntity.status(HttpStatus.CREATED).body(todoListReadModelResponse);
    }
}
