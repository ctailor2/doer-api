package com.doerapispring.web

import org.springframework.hateoas.RepresentationModel

case class TodoListReadModelDTO(name: String,
                                deferredName: String,
                                todos: List[TodoDTO],
                                deferredTodos: List[TodoDTO],
                                unlockDuration: Long) extends RepresentationModel[TodoListReadModelDTO]
