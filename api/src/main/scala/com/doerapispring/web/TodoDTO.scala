package com.doerapispring.web

import org.springframework.hateoas.ResourceSupport

case class TodoDTO(task: String) extends ResourceSupport
