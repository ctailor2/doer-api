package com.doerapispring.domain

import java.time.Clock
import java.util
import java.util.Date
import java.util.function.{BiFunction, Supplier}

import com.doerapispring.domain.events.TodoListEvent
import org.springframework.stereotype.Service

import scala.util.Try


@Service
class ListService(val completedTodoRepository: OwnedObjectReadRepository[CompletedTodoList, UserId, ListId],
                  val todoListRepository: OwnedObjectRepository[TodoList, UserId, ListId],
                  val todoListFactory: TodoListFactory,
                  val userRepository: ObjectRepository[User, UserId],
                  val todoListModelRepository: OwnedObjectReadRepository[DeprecatedTodoListModel, UserId, ListId],
                  val clock: Clock,
                  val domainEventPublisher: DomainEventPublisher[DeprecatedTodoListModel, TodoListEvent, UserId, ListId],
                  val todoListEventRepository: OwnedObjectWriteRepository[TodoListEvent, UserId, ListId],
                  val todoListModelSnapshotRepository: OwnedObjectWriteRepository[TodoListModelSnapshot, UserId, ListId])
  extends ListApplicationService {

  override def performOperation(user: User,
                                listId: ListId,
                                eventProducer: Supplier[TodoListEvent],
                                operation: BiFunction[DeprecatedTodoListModel, TodoListEvent, Try[DeprecatedTodoListModel]]): Try[DeprecatedTodoListModel] = {
    val todoListEvent = eventProducer.get()
    Try(todoListModelRepository.find(user.getUserId, listId).get)
      .flatMap(todoList => {
        operation.apply(todoList, todoListEvent)
      })
      .map(todoList => domainEventPublisher.publish(todoList, todoListEvent, user.getUserId, listId))
  }

  override def getDefault(user: User): DeprecatedTodoListModel = {
    todoListModelRepository.find(user.getUserId, user.getDefaultListId).get
  }

  override def getCompleted(user: User, listId: ListId): CompletedTodoList = {
    completedTodoRepository.find(user.getUserId, listId).get
  }

  override def get(user: User, listId: ListId): DeprecatedTodoListModel = {
    todoListModelRepository.find(user.getUserId, listId).get
  }

  override def getAll(user: User): util.List[TodoList] = {
    todoListRepository.findAll(user.getUserId)
  }

  override def create(user: User, name: String): Unit = {
    val listId = todoListRepository.nextIdentifier
    val todoList = todoListFactory.todoList(user.getUserId, listId, name)
    todoListRepository.save(todoList)
    todoListModelSnapshotRepository.save(
      user.getUserId,
      listId,
      TodoListModelSnapshot(DeprecatedTodoListModel(listId, name, List(), new Date(0L), 0), Date.from(clock.instant())))
  }

  override def setDefault(user: User, listId: ListId): Unit = {
    userRepository.save(new User(user.getUserId, listId))
  }
}
