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

case class DeprecatedEscalatedEvent() extends TodoListEvent

case class DeprecatedDeferredTodoAddedEvent(todoId: String,
                                            task: String) extends TodoListEvent

case class DeprecatedTodoMovedEvent(todoId: String,
                                    targetTodoId: String) extends TodoListEvent

case class DeprecatedPulledEvent() extends TodoListEvent

case class DeprecatedUnlockedEvent(unlockedAt: Date) extends TodoListEvent