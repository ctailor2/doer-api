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
class HomeController {
    private final HateoasLinkGenerator hateoasLinkGenerator;
    private final TodoApiService todoApiService;

    @Autowired
    HomeController(HateoasLinkGenerator hateoasLinkGenerator, TodoApiService todoApiService) {
        this.hateoasLinkGenerator = hateoasLinkGenerator;
        this.todoApiService = todoApiService;
    }

    @RequestMapping(value = "/home", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity<HomeResponse> home(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        // TODO: Move all of the links related to the main page to their own endpoint
        // so this endpoint truly becomes the root resources of the application
        try {
            MasterListDTO masterListDTO = todoApiService.get(authenticatedUser);
            HomeResponse homeResponse = new HomeResponse();
            homeResponse.add(hateoasLinkGenerator.homeLink().withSelfRel());
            homeResponse.add(hateoasLinkGenerator.todosLink().withRel("todos"));
            homeResponse.add(hateoasLinkGenerator.completedTodosLink().withRel("completedTodos"));
            homeResponse.add(hateoasLinkGenerator.createTodoForLaterLink().withRel("todoLater"));
            if (masterListDTO.isSchedulingForNowAllowed()) {
                homeResponse.add(hateoasLinkGenerator.pullTodosLink().withRel("pull"));
                homeResponse.add(hateoasLinkGenerator.createTodoForNowLink().withRel("todoNow"));
            }
            return ResponseEntity.status(HttpStatus.OK).body(homeResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
