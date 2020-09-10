package com.doerapispring.web

import java.util.Date

import com.doerapispring.domain.{DeprecatedTodoListModel, ListId, OwnedTodoListModel, TodoListModel}
import org.springframework.stereotype.Component

import scala.jdk.CollectionConverters._


@Component
class TodoListModelResourceTransformer(val hateoasLinkGenerator: HateoasLinkGenerator) {
  def transform(listId: ListId, todoListModel: TodoListModel, now: Date): TodoListReadModelResponse = {
    val capabilities = TodoListModel.capabilities(todoListModel)
    val todoListReadModelDTO = TodoListReadModelDTO(
      "now",
      "later",
      TodoListModel.getTodos(todoListModel).map(todo => TodoDTO(todo.task)),
      TodoListModel.getDeferredTodos(todoListModel, now).map(todo => TodoDTO(todo.task)),
      TodoListModel.unlockDurationMs(todoListModel, now)
    )
    null
//    capabilities.todoCapabilities.map(todoCapabilities =>
//      hateoasLinkGenerator.updateTodoLink("", todoCapabilities.update.apply("").index))
  }

  def transform(todoListModel: DeprecatedTodoListModel, now: Date): TodoListReadModelResponse = {
    val todoListReadModelDTO = new TodoListReadModelDTO(
      todoListModel.sectionName,
      todoListModel.deferredSectionName,
      DeprecatedTodoListModel.getTodos(todoListModel).map(todo => TodoDTO(todo.getTask)),
      DeprecatedTodoListModel.getDeferredTodos(todoListModel, now).map(todo => TodoDTO(todo.getTask)),
      DeprecatedTodoListModel.unlockDurationMs(todoListModel, now)
    )
    val listId = todoListModel.listId.get
    todoListReadModelDTO.add(hateoasLinkGenerator.createDeferredTodoLink(listId).withRel("createDeferred"))
    todoListReadModelDTO.add(hateoasLinkGenerator.completedListLink(listId).withRel("completed"))
    if (DeprecatedTodoListModel.unlockCapability(todoListModel, now).isSuccess) {
      todoListReadModelDTO.add(hateoasLinkGenerator.listUnlockLink(listId).withRel("unlock"))
    }
    if (DeprecatedTodoListModel.displaceCapability(todoListModel).isSuccess) {
      todoListReadModelDTO.add(hateoasLinkGenerator.displaceTodoLink(listId).withRel("displace"))
    }
    if (DeprecatedTodoListModel.addCapability(todoListModel).isSuccess) {
      todoListReadModelDTO.add(hateoasLinkGenerator.createTodoLink(listId).withRel("create"))
    }
    if (DeprecatedTodoListModel.pullCapability(todoListModel).isSuccess) {
      todoListReadModelDTO.add(hateoasLinkGenerator.listPullTodosLink(listId).withRel("pull"))
    }
    if (DeprecatedTodoListModel.escalateCapability(todoListModel).isSuccess) {
      todoListReadModelDTO.add(hateoasLinkGenerator.listEscalateTodoLink(listId).withRel("escalate"))
    }
//    TODO: Fix links
    todoListReadModelDTO.todos.foreach((todoDTO: TodoDTO) => {
      todoDTO.add(hateoasLinkGenerator.deleteTodoLink(listId, null).withRel("delete"))
      todoDTO.add(hateoasLinkGenerator.updateTodoLink(listId, null).withRel("update"))
      todoDTO.add(hateoasLinkGenerator.completeTodoLink(listId, null).withRel("complete"))
      todoListReadModelDTO.todos.foreach((targetTodoDTO: TodoDTO) => todoDTO.add(hateoasLinkGenerator.moveTodoLink(listId, null, null).withRel("move")))
    })
    todoListReadModelDTO.deferredTodos.foreach((todoDTO: TodoDTO) => {
      todoDTO.add(hateoasLinkGenerator.deleteTodoLink(listId, null).withRel("delete"))
      todoDTO.add(hateoasLinkGenerator.updateTodoLink(listId, null).withRel("update"))
      todoDTO.add(hateoasLinkGenerator.completeTodoLink(listId, null).withRel("complete"))
      todoListReadModelDTO.deferredTodos.foreach((targetTodoDTO: TodoDTO) => todoDTO.add(hateoasLinkGenerator.moveTodoLink(listId, null, null).withRel("move")))
    })
    val todoListReadModelResponse = new TodoListReadModelResponse(todoListReadModelDTO)
    todoListReadModelResponse
  }
}