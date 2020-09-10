package com.doerapispring.web

import org.springframework.hateoas.ResourceSupport

case class TodoListReadModelResponse(list: TodoListReadModelDTO) extends ResourceSupport
