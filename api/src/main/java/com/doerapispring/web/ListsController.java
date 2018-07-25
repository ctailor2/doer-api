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
class ListsController {
    private final HateoasLinkGenerator hateoasLinkGenerator;
    private final ListApiService listApiService;

    @Autowired
    ListsController(HateoasLinkGenerator hateoasLinkGenerator, ListApiService listApiService) {
        this.hateoasLinkGenerator = hateoasLinkGenerator;
        this.listApiService = listApiService;
    }

    @PostMapping(value = "/list/unlock")
    @ResponseBody
    ResponseEntity<ResourcesResponse> unlock(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        try {
            listApiService.unlock(authenticatedUser);
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
            MasterListDTO masterListDTO = listApiService.get(authenticatedUser);
            masterListDTO.add(hateoasLinkGenerator.createDeferredTodoLink().withRel("createDeferred"));
            masterListDTO.add(hateoasLinkGenerator.todosLink().withRel("todos"));
            if (masterListDTO.isAbleToBeUnlocked()) {
                masterListDTO.add(hateoasLinkGenerator.listUnlockLink().withRel("unlock"));
            }
            if (!masterListDTO.isLocked()) {
                masterListDTO.add(hateoasLinkGenerator.deferredTodosLink().withRel("deferredTodos"));
            }
            if (masterListDTO.isFull()) {
                masterListDTO.add(hateoasLinkGenerator.displaceTodoLink().withRel("displace"));
            } else {
                masterListDTO.add(hateoasLinkGenerator.createTodoLink().withRel("create"));
            }
            if (masterListDTO.isAbleToBeReplenished()) {
                masterListDTO.add(hateoasLinkGenerator.listPullTodosLink().withRel("pull"));
            }
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
            CompletedListDTO completedListDTO = listApiService.getCompleted(authenticatedUser);
            completedListDTO.add(hateoasLinkGenerator.completedTodosLink().withRel("todos"));
            CompletedListResponse completedListResponse = new CompletedListResponse(completedListDTO);
            completedListResponse.add(hateoasLinkGenerator.completedListLink().withSelfRel());
            return ResponseEntity.ok(completedListResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
