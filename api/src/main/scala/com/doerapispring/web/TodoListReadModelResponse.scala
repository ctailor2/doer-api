package com.doerapispring.web

import org.springframework.hateoas.RepresentationModel

case class TodoListReadModelResponse(list: TodoListReadModelDTO) extends RepresentationModel[TodoListReadModelResponse]
