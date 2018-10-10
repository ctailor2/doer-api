package com.doerapispring.web;

import com.doerapispring.authentication.AuthenticatedUser;
import com.doerapispring.domain.ListApplicationService;
import com.doerapispring.domain.ReadOnlyMasterList;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    ListsController(HateoasLinkGenerator hateoasLinkGenerator, ListApplicationService listApplicationService) {
        this.hateoasLinkGenerator = hateoasLinkGenerator;
        this.listApplicationService = listApplicationService;
    }

    @PostMapping(value = "/list/unlock")
    @ResponseBody
    ResponseEntity<ResourcesResponse> unlock(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        try {
            listApplicationService.unlock(authenticatedUser.getUser());
            ResourcesResponse resourcesResponse = new ResourcesResponse();
            resourcesResponse.add(
                hateoasLinkGenerator.listUnlockLink().withSelfRel(),
                hateoasLinkGenerator.listLink().withRel("list"));
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(resourcesResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @RequestMapping(value = "/list")
    @ResponseBody
    ResponseEntity<MasterListResponse> show(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        try {
            ReadOnlyMasterList readOnlyMasterList = listApplicationService.get(authenticatedUser.getUser());
            MasterListDTO masterListDTO = new MasterListDTO(
                readOnlyMasterList.getName(),
                readOnlyMasterList.getDeferredName(),
                readOnlyMasterList.getTodos().stream()
                    .map(todo -> new TodoDTO(
                        todo.getTodoId().getIdentifier(),
                        todo.getTask())).collect(toList()),
                readOnlyMasterList.getDeferredTodos().stream()
                    .map(todo -> new TodoDTO(
                        todo.getTodoId().getIdentifier(),
                        todo.getTask())).collect(toList()),
                readOnlyMasterList.unlockDuration(),
                readOnlyMasterList.isFull(),
                readOnlyMasterList.isAbleToBeUnlocked(),
                readOnlyMasterList.isAbleToBeReplenished());
            masterListDTO.add(hateoasLinkGenerator.createDeferredTodoLink().withRel("createDeferred"));
            if (masterListDTO.isAbleToBeUnlocked()) {
                masterListDTO.add(hateoasLinkGenerator.listUnlockLink().withRel("unlock"));
            }
            if (masterListDTO.isFull()) {
                masterListDTO.add(hateoasLinkGenerator.displaceTodoLink().withRel("displace"));
            } else {
                masterListDTO.add(hateoasLinkGenerator.createTodoLink().withRel("create"));
            }
            if (masterListDTO.isAbleToBeReplenished()) {
                masterListDTO.add(hateoasLinkGenerator.listPullTodosLink().withRel("pull"));
            }
            masterListDTO.getTodos().forEach(todoDTO -> {
                todoDTO.add(hateoasLinkGenerator.deleteTodoLink(todoDTO.getIdentifier()).withRel("delete"));
                todoDTO.add(hateoasLinkGenerator.updateTodoLink(todoDTO.getIdentifier()).withRel("update"));
                todoDTO.add(hateoasLinkGenerator.completeTodoLink(todoDTO.getIdentifier()).withRel("complete"));

                masterListDTO.getTodos().forEach(targetTodoDTO ->
                    todoDTO.add(hateoasLinkGenerator.moveTodoLink(
                        todoDTO.getIdentifier(),
                        targetTodoDTO.getIdentifier()).withRel("move")));
            });
            masterListDTO.getDeferredTodos().forEach(todoDTO -> {
                todoDTO.add(hateoasLinkGenerator.deleteTodoLink(todoDTO.getIdentifier()).withRel("delete"));
                todoDTO.add(hateoasLinkGenerator.updateTodoLink(todoDTO.getIdentifier()).withRel("update"));
                todoDTO.add(hateoasLinkGenerator.completeTodoLink(todoDTO.getIdentifier()).withRel("complete"));

                masterListDTO.getDeferredTodos().forEach(targetTodoDTO ->
                    todoDTO.add(hateoasLinkGenerator.moveTodoLink(
                        todoDTO.getIdentifier(),
                        targetTodoDTO.getIdentifier()).withRel("move")));
            });
            MasterListResponse masterListResponse = new MasterListResponse(masterListDTO);
            masterListResponse.add(hateoasLinkGenerator.listLink().withSelfRel());
            return ResponseEntity.ok(masterListResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping(value = "/completedList")
    @ResponseBody
    ResponseEntity<CompletedListResponse> showCompleted(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        try {
            CompletedListDTO completedListDTO = listApplicationService.getCompleted(authenticatedUser.getUser());
            CompletedListResponse completedListResponse = new CompletedListResponse(completedListDTO);
            completedListResponse.add(hateoasLinkGenerator.completedListLink().withSelfRel());
            return ResponseEntity.ok(completedListResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
