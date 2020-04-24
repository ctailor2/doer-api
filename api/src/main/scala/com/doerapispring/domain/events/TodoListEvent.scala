package com.doerapispring.domain.events

import java.util.Date

sealed trait TodoListEvent extends DomainEvent {
  def userId(): String
  def listId(): String
}

case class TodoUpdatedEvent(userId: String,
                            listId: String,
                            todoId: String,
                            task: String) extends TodoListEvent

case class TodoCompletedEvent(userId: String,
                              listId: String,
                              completedTodoId: String) extends TodoListEvent

case class TodoDisplacedEvent(userId: String,
                              listId: String,
                              todoId: String,
                              task: String) extends TodoListEvent

case class TodoDeletedEvent(userId: String,
                            listId: String,
                            todoId: String) extends TodoListEvent

case class TodoAddedEvent(userId: String,
                          listId: String,
                          todoId: String,
                          task: String) extends TodoListEvent

case class EscalatedEvent(userId: String,
                          listId: String) extends TodoListEvent

case class DeferredTodoAddedEvent(userId: String,
                                  listId: String,
                                  todoId: String,
                                  task: String) extends TodoListEvent

case class TodoMovedEvent(userId: String,
                          listId: String,
                          todoId: String,
                          targetTodoId: String) extends TodoListEvent

case class PulledEvent(userId: String,
                       listId: String) extends TodoListEvent

case class UnlockedEvent(userId: String,
                         listId: String,
                         unlockedAt: Date) extends TodoListEvent