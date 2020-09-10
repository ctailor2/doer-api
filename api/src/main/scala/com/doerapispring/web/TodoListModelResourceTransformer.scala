package com.doerapispring.web

import java.util.Date

import com.doerapispring.domain.{ListId, TodoListModel}
import org.springframework.stereotype.Component


@Component
class TodoListModelResourceTransformer(val hateoasLinkGenerator: HateoasLinkGenerator) {
  def transform(listId: ListId, todoListModel: TodoListModel, now: Date): TodoListReadModelResponse = {
    val capabilities = TodoListModel.capabilities(todoListModel, now)
    val todoListReadModelDTO = TodoListReadModelDTO(
      "now",
      "later",
      TodoListModel.getTodos(todoListModel).map(todo => TodoDTO(todo.task)),
      TodoListModel.getDeferredTodos(todoListModel, now).map(todo => TodoDTO(todo.task)),
      TodoListModel.unlockDurationMs(todoListModel, now)
    )
    todoListReadModelDTO.add(hateoasLinkGenerator.createDeferredTodoLink(listId.get()).withRel("createDeferred"))
    todoListReadModelDTO.add(hateoasLinkGenerator.completedListLink(listId.get()).withRel("completed"))
    if (capabilities.unlock.isDefined) {
      todoListReadModelDTO.add(hateoasLinkGenerator.listUnlockLink(listId.get()).withRel("unlock"))
    }
    if (capabilities.displace.isDefined) {
      todoListReadModelDTO.add(hateoasLinkGenerator.displaceTodoLink(listId.get()).withRel("displace"))
    }
    if (capabilities.add.isDefined) {
      todoListReadModelDTO.add(hateoasLinkGenerator.createTodoLink(listId.get()).withRel("create"))
    }
    if (capabilities.pull.isDefined) {
      todoListReadModelDTO.add(hateoasLinkGenerator.listPullTodosLink(listId.get()).withRel("pull"))
    }
    if (capabilities.escalate.isDefined) {
      todoListReadModelDTO.add(hateoasLinkGenerator.listEscalateTodoLink(listId.get()).withRel("escalate"))
    }
    todoListReadModelDTO.todos.zipWithIndex.foreach { case (todoDTO, index) =>
      val todoCapabilities = capabilities.todoCapabilities(index)
      todoDTO.add(hateoasLinkGenerator.deleteTodoLink(listId.get(), todoCapabilities.delete.index).withRel("delete"))
      todoDTO.add(hateoasLinkGenerator.updateTodoLink(listId.get(), todoCapabilities.update("").index).withRel("update"))
      todoDTO.add(hateoasLinkGenerator.completeTodoLink(listId.get(), todoCapabilities.complete(now).index).withRel("complete"))
      todoCapabilities.move.foreach(todoMovedEvent => {
        todoDTO.add(hateoasLinkGenerator.moveTodoLink(listId.get(), todoMovedEvent.index, todoMovedEvent.targetIndex).withRel("move"))
      })
    }
    todoListReadModelDTO.deferredTodos.zipWithIndex.foreach { case (todoDTO, index) =>
      val todoCapabilities = capabilities.deferredTodoCapabilities(index)
      todoDTO.add(hateoasLinkGenerator.deleteTodoLink(listId.get(), todoCapabilities.delete.index).withRel("delete"))
      todoDTO.add(hateoasLinkGenerator.updateTodoLink(listId.get(), todoCapabilities.update("").index).withRel("update"))
      todoDTO.add(hateoasLinkGenerator.completeTodoLink(listId.get(), todoCapabilities.complete(now).index).withRel("complete"))
      todoCapabilities.move.foreach(todoMovedEvent => {
        todoDTO.add(hateoasLinkGenerator.moveTodoLink(listId.get(), todoMovedEvent.index, todoMovedEvent.targetIndex).withRel("move"))
      })
    }
    val todoListReadModelResponse = TodoListReadModelResponse(todoListReadModelDTO)
    todoListReadModelResponse
  }
}