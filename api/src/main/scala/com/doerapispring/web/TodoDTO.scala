package com.doerapispring.web

import org.springframework.hateoas.RepresentationModel

case class TodoDTO(task: String) extends RepresentationModel[TodoDTO]
