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
public class TodosController {
    private final HateoasLinkGenerator hateoasLinkGenerator;
    private TodoApiService todoApiService;

    @Autowired
    public TodosController(HateoasLinkGenerator hateoasLinkGenerator, TodoApiService todoApiService) {
        this.hateoasLinkGenerator = hateoasLinkGenerator;
        this.todoApiService = todoApiService;
    }

    @RequestMapping(value = "/todos", method = RequestMethod.GET)
    @ResponseStatus(code = HttpStatus.OK)
    @ResponseBody
    ResponseEntity<TodosResponse> index(@AuthenticationPrincipal AuthenticatedUser authenticatedUser) {
        try {
            TodoList todoList = todoApiService.get(authenticatedUser);
            TodosResponse todosResponse = new TodosResponse(todoList.getTodoDTOs());
            todosResponse.add(hateoasLinkGenerator.todosLink().withSelfRel(),
                    hateoasLinkGenerator.createTodoForLaterLink().withRel("todoLater"));
            if (todoList.isSchedulingForNowAllowed()) {
                todosResponse.add(hateoasLinkGenerator.createTodoForNowLink().withRel("todoNow"));
            }
            return ResponseEntity.status(HttpStatus.OK)
                    .body(todosResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @RequestMapping(value = "/todos", method = RequestMethod.POST)
    @ResponseStatus(code = HttpStatus.CREATED)
    @ResponseBody
    ResponseEntity create(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                          @RequestBody TodoForm todoForm) {
        try {
            todoApiService.create(authenticatedUser, todoForm.getTask(), todoForm.getScheduling());
        } catch (InvalidRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(null);
    }

    @RequestMapping(value = "/todoNow", method = RequestMethod.POST)
    @ResponseStatus(code = HttpStatus.CREATED)
    @ResponseBody
    ResponseEntity<CreateTodoResponse> createForNow(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                @RequestBody SimpleTodoForm simpleTodoForm) {
        try {
            todoApiService.create(authenticatedUser, simpleTodoForm.getTask(), "now");
            CreateTodoResponse createTodoResponse = new CreateTodoResponse();
            createTodoResponse.add(hateoasLinkGenerator.createTodoForNowLink().withSelfRel(),
                    hateoasLinkGenerator.todosLink().withRel("todos"));
            return ResponseEntity.status(HttpStatus.CREATED).body(createTodoResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    @RequestMapping(value = "/todoLater", method = RequestMethod.POST)
    @ResponseStatus(code = HttpStatus.CREATED)
    @ResponseBody
    ResponseEntity<CreateTodoResponse> createForLater(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                         @RequestBody SimpleTodoForm simpleTodoForm) {
        try {
            todoApiService.create(authenticatedUser, simpleTodoForm.getTask(), "later");
            CreateTodoResponse createTodoResponse = new CreateTodoResponse();
            createTodoResponse.add(hateoasLinkGenerator.createTodoForLaterLink().withSelfRel(),
                    hateoasLinkGenerator.todosLink().withRel("todos"));
            return ResponseEntity.status(HttpStatus.CREATED).body(createTodoResponse);
        } catch (InvalidRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
