package com.doerapispring.domain

import com.doerapispring.domain.events.TodoListEvent
import org.springframework.stereotype.Service

@Service
class TodoService(private val todoListRepository: OwnedObjectReadRepository[TodoListValue, UserId, ListId],
                  private val todoListEventRepository: OwnedObjectWriteRepository[TodoListEvent, UserId, ListId],
                  private val todoRepository: IdentityGeneratingRepository[TodoId]) extends TodoApplicationService {

  override def create(user: User, listId: ListId, task: String): Unit = {
    todoListRepository.find(user.getUserId, listId)
      .map(todoList => TodoListValue.add(todoList, todoRepository.nextIdentifier(), task))
      .foreach { case (_, event) => todoListEventRepository.save(user.getUserId, listId, event) }
  }

  override def createDeferred(user: User, listId: ListId, task: String): Unit = {
    todoListRepository.find(user.getUserId, listId)
      .map(todoList => TodoListValue.addDeferred(todoList, todoRepository.nextIdentifier(), task))
      .foreach { case (_, event) => todoListEventRepository.save(user.getUserId, listId, event) }
  }

  override def delete(user: User, listId: ListId, todoId: TodoId): Unit = {
    todoListRepository.find(user.getUserId, listId)
      .map(todoList => TodoListValue.delete(todoList, todoId))
      .foreach { case (_, event) => todoListEventRepository.save(user.getUserId, listId, event) }
  }

  override def displace(user: User, listId: ListId, task: String): Unit = {
    todoListRepository.find(user.getUserId, listId)
      .map(todoList => TodoListValue.displace(todoList, todoRepository.nextIdentifier(), task))
      .foreach { case (_, event) => todoListEventRepository.save(user.getUserId, listId, event) }
  }

  override def update(user: User, listId: ListId, todoId: TodoId, task: String): Unit = {
    todoListRepository.find(user.getUserId, listId)
      .map(todoList => TodoListValue.update(todoList, todoId, task))
      .foreach { case (_, event) => todoListEventRepository.save(user.getUserId, listId, event) }
  }

  override def complete(user: User, listId: ListId, todoId: TodoId): Unit = {
    todoListRepository.find(user.getUserId, listId)
      .map(todoList => TodoListValue.complete(todoList, todoId))
      .foreach { case (_, event) => todoListEventRepository.save(user.getUserId, listId, event) }
  }

  override def move(user: User, listId: ListId, todoId: TodoId, targetTodoId: TodoId): Unit = {
    todoListRepository.find(user.getUserId, listId)
      .map(todoList => TodoListValue.move(todoList, todoId, targetTodoId))
      .foreach { case (_, event) => todoListEventRepository.save(user.getUserId, listId, event) }
  }

  override def pull(user: User, listId: ListId): Unit = {
    todoListRepository.find(user.getUserId, listId)
      .map(todoList => TodoListValue.pull(todoList))
      .foreach { case (_, event) => todoListEventRepository.save(user.getUserId, listId, event) }
  }

  override def escalate(user: User, listId: ListId): Unit = {
    todoListRepository.find(user.getUserId, listId)
      .map(todoList => TodoListValue.escalate(todoList))
      .foreach { case (_, event) => todoListEventRepository.save(user.getUserId, listId, event) }
  }
}
