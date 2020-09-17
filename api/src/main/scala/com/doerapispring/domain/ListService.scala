package com.doerapispring.domain

import java.time.Clock
import java.util
import java.util.Date

import com.doerapispring.domain.events.TodoListEvent
import org.springframework.stereotype.Service
import scala.jdk.CollectionConverters._

@Service
class ListService(val todoListRepository: OwnedObjectRepository[TodoList, UserId, ListId],
                  val todoListFactory: TodoListFactory,
                  val userRepository: ObjectRepository[User, UserId],
                  val todoListModelRepository: OwnedObjectReadRepository[TodoListModel, UserId, ListId],
                  val clock: Clock,
                  val domainEventPublisher: DomainEventPublisher[TodoListModel, TodoListEvent, UserId, ListId],
                  val todoListEventRepository: OwnedObjectWriteRepository[TodoListEvent, UserId, ListId],
                  val todoListModelSnapshotRepository: OwnedObjectWriteRepository[Snapshot[DeprecatedTodoListModel], UserId, ListId])
  extends ListApplicationService {

  override def performOperation(user: User,
                                listId: ListId,
                                event: TodoListEvent): TodoListModel = {
    todoListModelRepository.find(user.getUserId, listId)
      .map(todoList => TodoListModel.applyEvent(todoList, event))
      .map(todoList => domainEventPublisher.publish(todoList, event, user.getUserId, listId))
      .get
  }

  override def getDefault(user: User): TodoListModel = {
    todoListModelRepository.find(user.getUserId, user.getDefaultListId).get
  }

  override def getCompleted(user: User, listId: ListId): List[CompletedTodo] = {
    todoListModelRepository.find(user.getUserId, listId).get.completedTodos
  }

  override def get(user: User, listId: ListId): TodoListModel = {
    todoListModelRepository.find(user.getUserId, listId).get
  }

  override def getAll(user: User): List[TodoList] = {
    todoListRepository.findAll(user.getUserId).asScala.toList
  }

  override def create(user: User, name: String): Unit = {
    val listId = todoListRepository.nextIdentifier
    val todoList = todoListFactory.todoList(user.getUserId, listId, name)
    todoListRepository.save(todoList)
    todoListModelSnapshotRepository.save(
      user.getUserId,
      listId,
      Snapshot(DeprecatedTodoListModel(listId, name, List(), new Date(0L), 0), Date.from(clock.instant())))
  }

  override def setDefault(user: User, listId: ListId): Unit = {
    userRepository.save(new User(user.getUserId, listId))
  }
}
