package com.doerapispring.web;

import com.doerapispring.authentication.AuthenticatedUser;
import com.doerapispring.constants.AttributeName;
import com.doerapispring.domain.ListApplicationService;
import com.doerapispring.domain.TodoList;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
class ResourcesController {
    private final HateoasLinkGenerator hateoasLinkGenerator;
    private final ListApplicationService listApplicationService;
    private final AttributeName attributeName = AttributeName.FAT;

    ResourcesController(HateoasLinkGenerator hateoasLinkGenerator,
                        ListApplicationService listApplicationService) {
        this.hateoasLinkGenerator = hateoasLinkGenerator;
        this.listApplicationService = listApplicationService;
    }

    @RequestMapping(value = "/v1/", method = RequestMethod.GET)
    @ResponseBody
    ResponseEntity<ResourcesResponse> base() {
        ResourcesResponse resourcesResponse = new ResourcesResponse();
        resourcesResponse.add(
            hateoasLinkGenerator.baseResourcesLink().withSelfRel(),
            hateoasLinkGenerator.loginLink().withRel("login"),
            hateoasLinkGenerator.signupLink().withRel("signup"));
        return ResponseEntity.status(HttpStatus.OK).body(resourcesResponse);
    }

    @RequestMapping(value = "/v1/resources/root", method = RequestMethod.GET)
    @ResponseBody
    ResponseEntity<ResourcesResponse> root() {
        ResourcesResponse resourcesResponse = new ResourcesResponse();
        resourcesResponse.add(
            hateoasLinkGenerator.rootResourcesLink().withSelfRel(),
            hateoasLinkGenerator.listResourcesLink().withRel("listResources"),
            hateoasLinkGenerator.historyResourcesLink().withRel("historyResources"));
        return ResponseEntity.status(HttpStatus.OK).body(resourcesResponse);
    }

    @RequestMapping(value = "/v1/resources/list", method = RequestMethod.GET)
    @ResponseBody
    ResponseEntity<ResourcesResponse> list(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        TodoList defaultList = listApplicationService.getDefault(authenticatedUser.getUser());
        ResourcesResponse resourcesResponse = new ResourcesResponse();
        resourcesResponse.add(
            hateoasLinkGenerator.listResourcesLink().withSelfRel(),
            hateoasLinkGenerator.listLink(defaultList.getListId().get()).withRel("list"),
            hateoasLinkGenerator.showListsLink().withRel("lists"),
            hateoasLinkGenerator.createListsLink().withRel("createList"));
        return ResponseEntity.status(HttpStatus.OK).body(resourcesResponse);
    }

    @RequestMapping(value = "/v1/resources/history", method = RequestMethod.GET)
    @ResponseBody
    ResponseEntity<ResourcesResponse> history(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        TodoList defaultList = listApplicationService.getDefault(authenticatedUser.getUser());
        ResourcesResponse resourcesResponse = new ResourcesResponse();
        resourcesResponse.add(
            hateoasLinkGenerator.historyResourcesLink().withSelfRel(),
            hateoasLinkGenerator.completedListLink(defaultList.getListId().get()).withRel("completedList"));
        return ResponseEntity.status(HttpStatus.OK).body(resourcesResponse);
    }
}
