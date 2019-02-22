package com.doerapispring.web;

import com.doerapispring.authentication.AuthenticatedUser;
import com.doerapispring.domain.ListApplicationService;
import com.doerapispring.domain.ReadOnlyTodoList;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@CrossOrigin
class ResourcesController {
    private final HateoasLinkGenerator hateoasLinkGenerator;
    private final ListApplicationService listApplicationService;

    ResourcesController(HateoasLinkGenerator hateoasLinkGenerator,
                        ListApplicationService listApplicationService) {
        this.hateoasLinkGenerator = hateoasLinkGenerator;
        this.listApplicationService = listApplicationService;
    }

    @RequestMapping(value = {"/v1/", "/v1/resources/base"}, method = RequestMethod.GET)
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
            hateoasLinkGenerator.todoResourcesLink().withRel("todoResources"),
            hateoasLinkGenerator.historyResourcesLink().withRel("historyResources"));
        return ResponseEntity.status(HttpStatus.OK).body(resourcesResponse);
    }

    @RequestMapping(value = "/v1/resources/todo", method = RequestMethod.GET)
    @ResponseBody
    ResponseEntity<ResourcesResponse> todo(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) throws InvalidRequestException {
        ReadOnlyTodoList defaultList = listApplicationService.getDefault(authenticatedUser.getUser());
        ResourcesResponse resourcesResponse = new ResourcesResponse();
        resourcesResponse.add(
            hateoasLinkGenerator.todoResourcesLink().withSelfRel(),
            hateoasLinkGenerator.listLink(defaultList.getListId().get()).withRel("list"));
        return ResponseEntity.status(HttpStatus.OK).body(resourcesResponse);
    }

    @RequestMapping(value = "/v1/resources/history", method = RequestMethod.GET)
    @ResponseBody
    ResponseEntity<ResourcesResponse> history(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) throws InvalidRequestException {
        ReadOnlyTodoList defaultList = listApplicationService.getDefault(authenticatedUser.getUser());
        ResourcesResponse resourcesResponse = new ResourcesResponse();
        resourcesResponse.add(
            hateoasLinkGenerator.historyResourcesLink().withSelfRel(),
            hateoasLinkGenerator.completedListLink(defaultList.getListId().get()).withRel("completedList"));
        return ResponseEntity.status(HttpStatus.OK).body(resourcesResponse);
    }
}
