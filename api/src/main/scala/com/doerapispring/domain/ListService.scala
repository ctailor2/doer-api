package com.doerapispring.domain

import java.time.{Clock, Instant}
import java.util
import java.util.Date

import com.doerapispring.domain.events.TodoListEvent
import org.springframework.stereotype.Service

import scala.util.{Failure, Success}


@Service
class ListService(val completedTodoRepository: OwnedObjectRepository[CompletedTodoList, UserId, ListId],
                  val todoListRepository: OwnedObjectRepository[TodoList, UserId, ListId],
                  val todoListFactory: TodoListFactory,
                  val userRepository: ObjectRepository[User, UserId],
                  val todoListModelRepository: OwnedObjectReadRepository[TodoListModel, UserId, ListId],
                  val clock: Clock,
                  val todoListEventRepository: OwnedObjectWriteRepository[TodoListEvent, UserId, ListId])
  extends ListApplicationService {

  override def unlock(user: User, listId: ListId): Unit = {
    todoListModelRepository.find(user.getUserId, listId)
      .map(todoList => TodoListModel.unlock(todoList, Date.from(Instant.now)))
      .foreach {
        case Success((_, event)) => todoListEventRepository.save(user.getUserId, listId, event)
        case Failure(_) =>
      }
  }

  override def getDefault(user: User): TodoListModel = {
    todoListModelRepository.find(user.getUserId, user.getDefaultListId).get
  }

  override def getCompleted(user: User, listId: ListId): CompletedTodoList = {
    completedTodoRepository.find(user.getUserId, listId).orElseThrow(() => new ListNotFoundException)
  }

  override def get(user: User, listId: ListId): TodoListModel = {
    todoListModelRepository.find(user.getUserId, listId).get
  }

  override def getAll(user: User): util.List[TodoList] = {
    todoListRepository.findAll(user.getUserId)
  }

  override def create(user: User, name: String): Unit = {
    val listId = todoListRepository.nextIdentifier
    val todoList = todoListFactory.todoList(user.getUserId, listId, name)
    todoListRepository.save(todoList)
  }

  override def setDefault(user: User, listId: ListId): Unit = {
    userRepository.save(new User(user.getUserId, listId))
  }
}
