package com.doerapispring.domain.events

import java.util.Date

sealed trait TodoListEvent extends DomainEvent

case class TodoUpdatedEvent(todoId: String,
                            task: String) extends TodoListEvent

case class TodoCompletedEvent(completedTodoId: String) extends TodoListEvent

case class TodoDisplacedEvent(todoId: String,
                              task: String) extends TodoListEvent

case class TodoDeletedEvent(todoId: String) extends TodoListEvent

case class TodoAddedEvent(todoId: String,
                          task: String) extends TodoListEvent

case class EscalatedEvent() extends TodoListEvent

case class DeferredTodoAddedEvent(todoId: String,
                                  task: String) extends TodoListEvent

case class TodoMovedEvent(todoId: String,
                          targetTodoId: String) extends TodoListEvent

case class PulledEvent() extends TodoListEvent

case class UnlockedEvent(unlockedAt: Date) extends TodoListEvent