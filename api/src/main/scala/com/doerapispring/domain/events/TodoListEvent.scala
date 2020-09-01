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

case class DeprecatedDeferredTodoAddedEvent(todoId: String,
                                            task: String) extends TodoListEvent

case class DeprecatedTodoMovedEvent(todoId: String,
                                    targetTodoId: String) extends TodoListEvent

case class PulledEvent() extends TodoListEvent

case class UnlockedEvent(unlockedAt: Date) extends TodoListEvent