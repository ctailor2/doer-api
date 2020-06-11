package com.doerapispring.web

import java.util.Date

import com.doerapispring.domain.TodoListModel
import org.springframework.stereotype.Component

import scala.jdk.CollectionConverters._


@Component class TodoListModelResourceTransformer(val hateoasLinkGenerator: HateoasLinkGenerator) {
  def transform(todoListModel: TodoListModel, now: Date): TodoListReadModelResponse = {
    val todoListReadModelDTO = new TodoListReadModelDTO(
      todoListModel.profileName,
      todoListModel.sectionName,
      todoListModel.deferredSectionName,
      TodoListModel.getTodos(todoListModel).map(todo => new TodoDTO(todo.getTodoId.getIdentifier, todo.getTask)).asJava,
      TodoListModel.getDeferredTodos(todoListModel, now).map(todo => new TodoDTO(todo.getTodoId.getIdentifier, todo.getTask)).asJava,
      TodoListModel.unlockDurationMs(todoListModel, now)
    )
    val listId = todoListModel.listId.get
    todoListReadModelDTO.add(hateoasLinkGenerator.createDeferredTodoLink(listId).withRel("createDeferred"))
    todoListReadModelDTO.add(hateoasLinkGenerator.completedListLink(listId).withRel("completed"))
    if (TodoListModel.unlockCapability(todoListModel, now).isSuccess) {
      todoListReadModelDTO.add(hateoasLinkGenerator.listUnlockLink(listId).withRel("unlock"))
    }
    if (TodoListModel.displaceCapability(todoListModel).isSuccess) {
      todoListReadModelDTO.add(hateoasLinkGenerator.displaceTodoLink(listId).withRel("displace"))
    }
    if (TodoListModel.addCapability(todoListModel).isSuccess) {
      todoListReadModelDTO.add(hateoasLinkGenerator.createTodoLink(listId).withRel("create"))
    }
    if (TodoListModel.pullCapability(todoListModel).isSuccess) {
      todoListReadModelDTO.add(hateoasLinkGenerator.listPullTodosLink(listId).withRel("pull"))
    }
    if (TodoListModel.escalateCapability(todoListModel).isSuccess) {
      todoListReadModelDTO.add(hateoasLinkGenerator.listEscalateTodoLink(listId).withRel("escalate"))
    }
    todoListReadModelDTO.getTodos.forEach((todoDTO: TodoDTO) => {
      todoDTO.add(hateoasLinkGenerator.deleteTodoLink(listId, todoDTO.getIdentifier).withRel("delete"))
      todoDTO.add(hateoasLinkGenerator.updateTodoLink(listId, todoDTO.getIdentifier).withRel("update"))
      todoDTO.add(hateoasLinkGenerator.completeTodoLink(listId, todoDTO.getIdentifier).withRel("complete"))
      todoListReadModelDTO.getTodos.forEach((targetTodoDTO: TodoDTO) => todoDTO.add(hateoasLinkGenerator.moveTodoLink(listId, todoDTO.getIdentifier, targetTodoDTO.getIdentifier).withRel("move")))
    })
    todoListReadModelDTO.getDeferredTodos.forEach((todoDTO: TodoDTO) => {
      todoDTO.add(hateoasLinkGenerator.deleteTodoLink(listId, todoDTO.getIdentifier).withRel("delete"))
      todoDTO.add(hateoasLinkGenerator.updateTodoLink(listId, todoDTO.getIdentifier).withRel("update"))
      todoDTO.add(hateoasLinkGenerator.completeTodoLink(listId, todoDTO.getIdentifier).withRel("complete"))
      todoListReadModelDTO.getDeferredTodos.forEach((targetTodoDTO: TodoDTO) => todoDTO.add(hateoasLinkGenerator.moveTodoLink(listId, todoDTO.getIdentifier, targetTodoDTO.getIdentifier).withRel("move")))
    })
    val todoListReadModelResponse = new TodoListReadModelResponse(todoListReadModelDTO)
    todoListReadModelResponse.add(hateoasLinkGenerator.listLink(listId).withSelfRel)
    todoListReadModelResponse
  }
}