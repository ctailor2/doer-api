package com.doerapispring.web;

import com.doerapispring.authentication.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin
@RequestMapping(value = "/v1")
public class TodosController {
    private TodoApiService todoApiService;

    @Autowired
    public TodosController(TodoApiService todoApiService) {
        this.todoApiService = todoApiService;
    }

    @RequestMapping(value = "/todos", method = RequestMethod.GET)
    @ResponseStatus(code = HttpStatus.OK)
    @ResponseBody
    ResponseEntity<List<TodoDTO>> index(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                                        @RequestParam(name = "scheduling", required = false, defaultValue = "anytime") String scheduling) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(todoApiService.getByScheduling(authenticatedUser, scheduling));
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
            return ResponseEntity.status(HttpStatus.CREATED).body(null);
        } catch (InvalidRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }
}
