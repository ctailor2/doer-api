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
    private final TodoListReadModelResourceTransformer todoListReadModelResourceTransformer;

    ListsController(HateoasLinkGenerator hateoasLinkGenerator,
                    ListApplicationService listApplicationService,
                    TodoListReadModelResourceTransformer todoListReadModelResourceTransformer) {
        this.hateoasLinkGenerator = hateoasLinkGenerator;
        this.listApplicationService = listApplicationService;
        this.todoListReadModelResourceTransformer = todoListReadModelResourceTransformer;
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

    @RequestMapping("/lists/default")
    @ResponseBody
    ResponseEntity<TodoListReadModelResponse> showDefault(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        TodoListReadModel todoListReadModel = listApplicationService.getDefault(authenticatedUser.getUser());
        TodoListReadModelResponse todoListReadModelResponse = todoListReadModelResourceTransformer.transform(todoListReadModel);
        return ResponseEntity.ok(todoListReadModelResponse);
    }

    @RequestMapping("/lists/{listId}")
    @ResponseBody
    ResponseEntity<TodoListReadModelResponse> show(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                                   @PathVariable String listId) {
        TodoListReadModel todoListReadModel = listApplicationService.get(authenticatedUser.getUser(), new ListId(listId));
        TodoListReadModelResponse todoListReadModelResponse = todoListReadModelResourceTransformer.transform(todoListReadModel);
        return ResponseEntity.ok(todoListReadModelResponse);
    }

    @GetMapping(value = "/lists/{listId}/completed")
    @ResponseBody
    ResponseEntity<CompletedListResponse> showCompleted(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                                        @PathVariable String listId) {
        CompletedListDTO completedListDTO = new CompletedListDTO(
            listApplicationService.getCompleted(authenticatedUser.getUser(), new ListId(listId)).getTodos().stream()
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
//                TODO: Add the completed list link
                return todoListDTO;
            })
            .collect(toList());
        TodoListResponse todoListResponse = new TodoListResponse(todoListDTOS);
        todoListResponse.add(hateoasLinkGenerator.showListsLink());
        return ResponseEntity.ok(todoListResponse);
    }

    @PostMapping(value = "/lists/{listId}/default")
    @ResponseBody
    ResponseEntity<ResourcesResponse> setDefault(@AuthenticationPrincipal AuthenticatedUser authenticatedUser, @PathVariable String listId) {
        listApplicationService.setDefault(authenticatedUser.getUser(), new ListId(listId));
        ResourcesResponse resourcesResponse = new ResourcesResponse();
        resourcesResponse.add(hateoasLinkGenerator.setDefaultListLink(listId).withSelfRel());
        resourcesResponse.add(hateoasLinkGenerator.listLink(listId).withRel("list"));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(resourcesResponse);
    }

    @PostMapping(value = "/lists")
    @ResponseBody
    ResponseEntity<ResourcesResponse> create(
        @AuthenticationPrincipal AuthenticatedUser authenticatedUser,
        @RequestBody ListForm listForm) {
        listApplicationService.create(authenticatedUser.getUser(), listForm.getName());
        ResourcesResponse resourcesResponse = new ResourcesResponse();
        resourcesResponse.add(hateoasLinkGenerator.createListsLink().withSelfRel());
        resourcesResponse.add(hateoasLinkGenerator.showListsLink().withRel("lists"));
        return ResponseEntity.status(HttpStatus.CREATED).body(resourcesResponse);
    }
}
