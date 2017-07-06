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
    private ListApiService listApiService;

    @Autowired
    ListsController(HateoasLinkGenerator hateoasLinkGenerator, ListApiService listApiService) {
        this.hateoasLinkGenerator = hateoasLinkGenerator;
        this.listApiService = listApiService;
    }

    @RequestMapping(value = "/list/unlock", method = RequestMethod.POST)
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
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @RequestMapping(value = "/list", method = RequestMethod.GET)
    @ResponseBody
    ResponseEntity<ListResponse> show(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        try {
            // TODO: Either add todo links here or separate it out to a todos index endpoint
            MasterListDTO masterListDTO = listApiService.get(authenticatedUser);
            if (!masterListDTO.isFull()) {
                masterListDTO.add(hateoasLinkGenerator.createTodoLink().withRel("create"));
                masterListDTO.add(hateoasLinkGenerator.listPullTodosLink().withRel("pull"));
            }
            ListResponse listResponse = new ListResponse(masterListDTO);
            listResponse.add(hateoasLinkGenerator.listLink().withSelfRel());
            return ResponseEntity.ok(listResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    // TODO: Need to start treating the lists as their own resources
    // Lists can be locked or unlocked
    // When unlocked, they have a property indicating the time at which they will be locked again
    // When locked, they have a property indicating when they can be locked again
}