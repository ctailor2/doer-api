package com.doerapispring.todos;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
    List<TodoEntity> index(@RequestHeader(value = "Session-Token") String token) {
        return todoService.get(token);
    }

    @RequestMapping(value = "/todos", method = RequestMethod.POST)
    @ResponseStatus(code = HttpStatus.CREATED)
    @ResponseBody
    TodoEntity create(@RequestHeader(value = "Session-Token") String token,
                      @RequestBody TodoEntity todoEntity) {
        return todoService.create(token, todoEntity);
    }
}
