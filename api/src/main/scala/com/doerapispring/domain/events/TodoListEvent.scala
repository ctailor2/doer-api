package com.doerapispring.domain.events

import java.util.Date

sealed trait TodoListEvent extends DomainEvent

case class DeprecatedTodoUpdatedEvent(todoId: String,
                                      task: String) extends TodoListEvent

case class DeprecatedTodoCompletedEvent(completedTodoId: String) extends TodoListEvent

case class DeprecatedTodoDisplacedEvent(todoId: String,
                                        task: String) extends TodoListEvent

case class DeprecatedTodoDeletedEvent(todoId: String) extends TodoListEvent

case class DeprecatedTodoAddedEvent(todoId: String,
                                    task: String) extends TodoListEvent

case class EscalatedEvent() extends TodoListEvent

case class DeprecatedEscalatedEvent() extends TodoListEvent

case class DeprecatedDeferredTodoAddedEvent(todoId: String,
                                            task: String) extends TodoListEvent

case class DeprecatedTodoMovedEvent(todoId: String,
                                    targetTodoId: String) extends TodoListEvent

case class PulledEvent() extends TodoListEvent

case class DeprecatedPulledEvent() extends TodoListEvent

case class UnlockedEvent(unlockedAt: Date) extends TodoListEvent

case class TodoUpdatedEvent(index: Int, task: String) extends TodoListEvent

case class TodoCompletedEvent(index: Int, completedAt: Date) extends TodoListEvent

case class TodoDisplacedEvent(task: String) extends TodoListEvent


case class TodoDeletedEvent(index: Int) extends TodoListEvent

case class TodoAddedEvent(task: String) extends TodoListEvent

case class DeferredTodoAddedEvent(task: String) extends TodoListEvent

case class TodoMovedEvent(index: Int, targetIndex: Int) extends TodoListEvent

case class DeprecatedUnlockedEvent(unlockedAt: Date) extends TodoListEvent