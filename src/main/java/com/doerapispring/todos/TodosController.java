package com.doerapispring.todos;

import com.doerapispring.apiTokens.AuthenticatedUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Created by chiragtailor on 9/27/16.
 */
@RestController
@CrossOrigin
@RequestMapping(value = "/v1")
public class TodosController {
    private TodoService todoService;

    @Autowired
    public TodosController(TodoService todoService) {
        this.todoService = todoService;
    }

    @RequestMapping(value = "/todos", method = RequestMethod.GET)
    @ResponseStatus(code = HttpStatus.OK)
    @ResponseBody
    List<Todo> index(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                     @RequestParam(name = "type", required = false) TodoTypeParamEnum todoTypeParamEnum) {
        return todoService.get(authenticatedUser.getUserIdentifier().get(), todoTypeParamEnum);
    }

    @RequestMapping(value = "/todos", method = RequestMethod.POST)
    @ResponseStatus(code = HttpStatus.CREATED)
    @ResponseBody
    Todo create(@AuthenticationPrincipal AuthenticatedUser authenticatedUser,
                @RequestBody Todo todo) {
        return todoService.create(authenticatedUser.getUserIdentifier().get(), todo);
    }
}
