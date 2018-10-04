package com.doerapispring.web;

import com.doerapispring.authentication.AuthenticatedUser;
import com.doerapispring.domain.TodoApplicationService;
import com.doerapispring.domain.TodoId;
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
    private final TodoApplicationService todoApplicationService;

    @Autowired
    TodosController(HateoasLinkGenerator hateoasLinkGenerator, TodoApplicationService todoApplicationService) {
        this.hateoasLinkGenerator = hateoasLinkGenerator;
        this.todoApplicationService = todoApplicationService;
    }

    @RequestMapping(value = "/todos/{localId}", method = RequestMethod.DELETE)
    @ResponseBody
    ResponseEntity<ResourcesResponse> delete(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                             @PathVariable(value = "localId") String localId) {
        try {
            todoApplicationService.delete(authenticatedUser.getUser(), new TodoId(localId));
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
        todoApplicationService.displace(authenticatedUser.getUser(), todoForm.getTask());
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
        todoApplicationService.update(authenticatedUser.getUser(), new TodoId(localId), todoForm.getTask());
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
            todoApplicationService.complete(authenticatedUser.getUser(), new TodoId(localId));
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
            todoApplicationService.move(authenticatedUser.getUser(), new TodoId(localId), new TodoId(targetLocalId));
            ResourcesResponse resourcesResponse = new ResourcesResponse();
            resourcesResponse.add(
                hateoasLinkGenerator.moveTodoLink(localId, targetLocalId).withSelfRel(),
                hateoasLinkGenerator.listLink().withRel("list"));
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(resourcesResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @RequestMapping(value = "/list/pull", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<ResourcesResponse> pull(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        try {
            todoApplicationService.pull(authenticatedUser.getUser());
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
        todoApplicationService.create(authenticatedUser.getUser(), todoForm.getTask());
        ResourcesResponse resourcesResponse = new ResourcesResponse();
        resourcesResponse.add(
            hateoasLinkGenerator.createTodoLink().withSelfRel(),
            hateoasLinkGenerator.listLink().withRel("list"));
        return ResponseEntity.status(HttpStatus.CREATED).body(resourcesResponse);
    }

    @RequestMapping(value = "/list/deferredTodos", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<ResourcesResponse> createDeferred(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                                     @RequestBody TodoForm todoForm) throws InvalidRequestException {
        todoApplicationService.createDeferred(authenticatedUser.getUser(), todoForm.getTask());
        ResourcesResponse resourcesResponse = new ResourcesResponse();
        resourcesResponse.add(
            hateoasLinkGenerator.createDeferredTodoLink().withSelfRel(),
            hateoasLinkGenerator.listLink().withRel("list"));
        return ResponseEntity.status(HttpStatus.CREATED).body(resourcesResponse);
    }
}
