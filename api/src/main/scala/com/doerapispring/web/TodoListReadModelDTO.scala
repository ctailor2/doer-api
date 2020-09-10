package com.doerapispring.web

import com.doerapispring.domain.Todo
import org.springframework.hateoas.ResourceSupport

case class TodoListReadModelDTO(name: String,
                                deferredName: String,
                                todos: List[TodoDTO],
                                deferredTodos: List[TodoDTO],
                                unlockDuration: Long) extends ResourceSupport
