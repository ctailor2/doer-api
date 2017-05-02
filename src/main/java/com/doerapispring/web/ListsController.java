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

    @RequestMapping(value = "/lists/unlock", method = RequestMethod.POST)
    @ResponseBody
    ResponseEntity<ResourcesResponse> unlock(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        try {
            listApiService.unlock(authenticatedUser);
            ResourcesResponse resourcesResponse = new ResourcesResponse();
            resourcesResponse.add(
                    hateoasLinkGenerator.unlockListLink().withSelfRel(),
                    hateoasLinkGenerator.todosLink("later").withRel("laterTodos"));
            return ResponseEntity.status(HttpStatus.ACCEPTED).body(resourcesResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
