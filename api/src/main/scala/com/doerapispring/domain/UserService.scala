package com.doerapispring.domain

import java.time.Clock
import java.util.{Date, Optional}

import org.springframework.stereotype.Service

@Service
class UserService(val userRepository: ObjectRepository[User, UserId],
                  val todoListFactory: TodoListFactory,
                  val todoListRepository: OwnedObjectRepository[TodoList, UserId, ListId],
                  val todoListModelSnapshotRepository: OwnedObjectWriteRepository[Snapshot[DeprecatedTodoListModel], UserId, ListId],
                  val clock: Clock) {
  def create(identifier: String): User = {
    val userId = new UserId(identifier)
    val userOptional = userRepository.find(userId)
    if (userOptional.isPresent) throw new UserAlreadyExistsException
    val todoList = todoListFactory.todoList(
      userId,
      todoListRepository.nextIdentifier(),
      "default")
    val listId = todoList.getListId
    val user = new User(userId, listId)
    userRepository.save(user)
    todoListRepository.save(todoList)
    todoListModelSnapshotRepository.save(
      user.getUserId,
      listId,
      Snapshot(DeprecatedTodoListModel(listId, todoList.getName, List(), new Date(0L), 0), Date.from(clock.instant())))
    user
  }

  def find(identifier: String): Optional[User] = {
    userRepository.find(new UserId(identifier))
  }
}