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

    @RequestMapping(value = "/lists/{listId}/todos/{index}", method = RequestMethod.DELETE)
    @ResponseBody
    ResponseEntity<TodoListReadModelResponse> delete(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                                     @PathVariable String listId,
                                                     @PathVariable int index) {
        TodoListModel todoListModel = todoApplicationService.performOperation(
                authenticatedUser.getUser(),
                new ListId(listId),
                new TodoDeletedEvent(index));
        TodoListReadModelResponse todoListReadModelResponse = todoListModelResourceTransformer.transform(new ListId(listId), todoListModel, Date.from(clock.instant()));
        todoListReadModelResponse.add(hateoasLinkGenerator.deleteTodoLink(listId, index).withSelfRel(),
                hateoasLinkGenerator.listLink(listId).withRel("list"));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(todoListReadModelResponse);
    }

    @RequestMapping(value = "/lists/{listId}/displace", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<TodoListReadModelResponse> displace(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                                       @PathVariable String listId,
                                                       @RequestBody TodoForm todoForm) {
        TodoListModel todoListModel = todoApplicationService.performOperation(
                authenticatedUser.getUser(),
                new ListId(listId),
                new TodoDisplacedEvent(todoForm.getTask()));
        TodoListReadModelResponse todoListReadModelResponse =
                todoListModelResourceTransformer.transform(new ListId(listId), todoListModel, Date.from(clock.instant()));
        todoListReadModelResponse.add(hateoasLinkGenerator.displaceTodoLink(listId).withSelfRel(),
                hateoasLinkGenerator.listLink(listId).withRel("list"));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(todoListReadModelResponse);
    }

    @RequestMapping(value = "/lists/{listId}/todos/{index}", method = RequestMethod.PUT)
    @ResponseBody
    ResponseEntity<TodoListReadModelResponse> update(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                                     @PathVariable String listId,
                                                     @PathVariable int index,
                                                     @RequestBody TodoForm todoForm) {
        TodoListModel todoListModel = todoApplicationService.performOperation(
                authenticatedUser.getUser(),
                new ListId(listId),
                new TodoUpdatedEvent(index, todoForm.getTask()));
        TodoListReadModelResponse todoListReadModelResponse =
                todoListModelResourceTransformer.transform(new ListId(listId), todoListModel, Date.from(clock.instant()));
        todoListReadModelResponse.add(hateoasLinkGenerator.updateTodoLink(listId, index).withSelfRel(),
                hateoasLinkGenerator.listLink(listId).withRel("list"));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(todoListReadModelResponse);
    }

    @RequestMapping(value = "/lists/{listId}/todos/{index}/complete", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<TodoListReadModelResponse> complete(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                                       @PathVariable String listId,
                                                       @PathVariable int index) {
        TodoListModel todoListModel = todoApplicationService.performOperation(
                authenticatedUser.getUser(),
                new ListId(listId),
                new TodoCompletedEvent(index, Date.from(clock.instant())));
        TodoListReadModelResponse todoListReadModelResponse =
                todoListModelResourceTransformer.transform(new ListId(listId), todoListModel, Date.from(clock.instant()));
        todoListReadModelResponse.add(
                hateoasLinkGenerator.completeTodoLink(listId, index).withSelfRel(),
                hateoasLinkGenerator.listLink(listId).withRel("list"));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(todoListReadModelResponse);
    }

    @RequestMapping(value = "/lists/{listId}/todos/{index}/move/{targetIndex}", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<TodoListReadModelResponse> move(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                                   @PathVariable String listId,
                                                   @PathVariable int index,
                                                   @PathVariable int targetIndex) {
        TodoListModel todoListModel = todoApplicationService.performOperation(
                authenticatedUser.getUser(),
                new ListId(listId),
                new TodoMovedEvent(index, targetIndex));
        TodoListReadModelResponse todoListReadModelResponse =
                todoListModelResourceTransformer.transform(new ListId(listId), todoListModel, Date.from(clock.instant()));
        todoListReadModelResponse.add(
                hateoasLinkGenerator.moveTodoLink(listId, index, targetIndex).withSelfRel(),
                hateoasLinkGenerator.listLink(listId).withRel("list"));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(todoListReadModelResponse);
    }

    @RequestMapping(value = "/lists/{listId}/pull", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<TodoListReadModelResponse> pull(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                                   @PathVariable String listId) {
        TodoListModel todoListModel = todoApplicationService.performOperation(
                authenticatedUser.getUser(),
                new ListId(listId),
                new PulledEvent());
        TodoListReadModelResponse todoListReadModelResponse =
                todoListModelResourceTransformer.transform(new ListId(listId), todoListModel, Date.from(clock.instant()));
        todoListReadModelResponse.add(
                hateoasLinkGenerator.listPullTodosLink(listId).withSelfRel(),
                hateoasLinkGenerator.listLink(listId).withRel("list"));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(todoListReadModelResponse);
    }

    @RequestMapping(value = "/lists/{listId}/escalate", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<TodoListReadModelResponse> escalate(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                                       @PathVariable String listId) {
        TodoListModel todoListModel = todoApplicationService.performOperation(
                authenticatedUser.getUser(),
                new ListId(listId),
                new EscalatedEvent());
        TodoListReadModelResponse todoListReadModelResponse =
                todoListModelResourceTransformer.transform(new ListId(listId), todoListModel, Date.from(clock.instant()));
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
        TodoListModel todoListModel = todoApplicationService.performOperation(
                authenticatedUser.getUser(),
                new ListId(listId),
                new TodoAddedEvent(todoForm.getTask()));
        TodoListReadModelResponse todoListReadModelResponse =
                todoListModelResourceTransformer.transform(new ListId(listId), todoListModel, Date.from(clock.instant()));
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
        TodoListModel todoListModel = todoApplicationService.performOperation(
                authenticatedUser.getUser(),
                new ListId(listId),
                new DeferredTodoAddedEvent(todoForm.getTask()));
        TodoListReadModelResponse todoListReadModelResponse =
                todoListModelResourceTransformer.transform(new ListId(listId), todoListModel, Date.from(clock.instant()));
        todoListReadModelResponse.add(
                hateoasLinkGenerator.createDeferredTodoLink(listId).withSelfRel(),
                hateoasLinkGenerator.listLink(listId).withRel("list"));
        return ResponseEntity.status(HttpStatus.CREATED).body(todoListReadModelResponse);
    }
}
