package com.doerapispring.web;

import com.doerapispring.authentication.AuthenticatedUser;
import com.doerapispring.domain.ListApplicationService;
import com.doerapispring.domain.ListId;
import com.doerapispring.domain.TodoListModel;
import com.doerapispring.domain.User;
import com.doerapispring.domain.events.UnlockedEvent;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.Clock;
import java.util.Date;
import java.util.List;

import static java.util.stream.Collectors.toList;
import static scala.jdk.javaapi.CollectionConverters.asJava;

@RestController
@CrossOrigin
@RequestMapping(value = "/v1")
class ListsController {
    private final HateoasLinkGenerator hateoasLinkGenerator;
    private final ListApplicationService listApplicationService;
    private final TodoListModelResourceTransformer todoListModelResourceTransformer;
    private final Clock clock;

    ListsController(HateoasLinkGenerator hateoasLinkGenerator,
                    ListApplicationService listApplicationService,
                    TodoListModelResourceTransformer todoListModelResourceTransformer,
                    Clock clock) {
        this.hateoasLinkGenerator = hateoasLinkGenerator;
        this.listApplicationService = listApplicationService;
        this.todoListModelResourceTransformer = todoListModelResourceTransformer;
        this.clock = clock;
    }

    @PostMapping(value = "/lists/{listId}/unlock")
    @ResponseBody
    ResponseEntity<TodoListReadModelResponse> unlock(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                             @PathVariable String listId) {
        TodoListModel todoListModel = listApplicationService.performOperation(
                authenticatedUser.getUser(),
                new ListId(listId),
                new UnlockedEvent(Date.from(clock.instant())));
        TodoListReadModelResponse todoListReadModelResponse = todoListModelResourceTransformer.transform(new ListId(listId), todoListModel, Date.from(clock.instant()));
        todoListReadModelResponse.add(
                hateoasLinkGenerator.listUnlockLink(listId).withSelfRel(),
                hateoasLinkGenerator.listLink(listId).withRel("list"));
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(todoListReadModelResponse);
    }

    @RequestMapping("/lists/default")
    @ResponseBody
    ResponseEntity<TodoListReadModelResponse> showDefault(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        User user = authenticatedUser.getUser();
        TodoListModel todoListModel = listApplicationService.getDefault(user);
        TodoListReadModelResponse todoListReadModelResponse = todoListModelResourceTransformer.transform(user.getDefaultListId(), todoListModel, Date.from(clock.instant()));
        todoListReadModelResponse.add(hateoasLinkGenerator.listLink(user.getDefaultListId().get()).withSelfRel());
        return ResponseEntity.ok(todoListReadModelResponse);
    }

    @RequestMapping("/lists/{listId}")
    @ResponseBody
    ResponseEntity<TodoListReadModelResponse> show(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                                   @PathVariable String listId) {
        TodoListModel todoListModel = listApplicationService.get(authenticatedUser.getUser(), new ListId(listId));
        TodoListReadModelResponse todoListReadModelResponse = todoListModelResourceTransformer.transform(new ListId(listId), todoListModel, Date.from(clock.instant()));
        todoListReadModelResponse.add(hateoasLinkGenerator.listLink(listId).withSelfRel());
        return ResponseEntity.ok(todoListReadModelResponse);
    }

    @GetMapping(value = "/lists/{listId}/completed")
    @ResponseBody
    ResponseEntity<CompletedListResponse> showCompleted(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                                        @PathVariable String listId) {
        CompletedListDTO completedListDTO = new CompletedListDTO(
                asJava(listApplicationService.getCompleted(authenticatedUser.getUser(), new ListId(listId)))
                        .stream()
                        .map(completedTodo -> new CompletedTodoDTO(completedTodo.task(), completedTodo.completedAt()))
                        .collect(toList()));
        CompletedListResponse completedListResponse = new CompletedListResponse(completedListDTO);
        completedListResponse.add(hateoasLinkGenerator.completedListLink(listId).withSelfRel());
        return ResponseEntity.ok(completedListResponse);
    }

    @GetMapping(value = "/lists")
    @ResponseBody
    ResponseEntity<TodoListResponse> showAll(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        List<TodoListDTO> todoListDTOS = asJava(listApplicationService.getAll(authenticatedUser.getUser())).stream()
                .map(todoList -> {
                    TodoListDTO todoListDTO = new TodoListDTO(todoList.getName());
                    todoListDTO.add(hateoasLinkGenerator.listLink(todoList.getListId().get()).withRel("list"));
                    todoListDTO.add(hateoasLinkGenerator.setDefaultListLink(todoList.getListId().get()).withRel("setDefault"));
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
