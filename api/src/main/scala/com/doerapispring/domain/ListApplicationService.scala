package com.doerapispring.domain

import com.doerapispring.domain.events.TodoListEvent

trait ListApplicationService {
  def performOperation(user: User, listId: ListId, event: TodoListEvent): TodoListModel

  def getDefault(user: User): TodoListModel

  def getCompleted(user: User, listId: ListId): List[CompletedTodo]

  def get(user: User, listId: ListId): TodoListModel

  def getAll(user: User): List[TodoList]

  def create(user: User, name: String): Unit

  def setDefault(user: User, listId: ListId): Unit
}