package com.doerapispring.domain

import java.util.Optional

import org.springframework.stereotype.Service

@Service
class UserService(val userRepository: ObjectRepository[User, UserId],
                  val todoListFactory: TodoListFactory,
                  val todoListRepository: OwnedObjectRepository[TodoList, UserId, ListId]) {
  def create(identifier: String): User = {
    val userId = new UserId(identifier)
    val userOptional = userRepository.find(userId)
    if (userOptional.isPresent) throw new UserAlreadyExistsException
    val todoList = todoListFactory.todoList(
      userId,
      todoListRepository.nextIdentifier(),
      "default")
    val user = new User(userId, todoList.getListId)
    userRepository.save(user)
    todoListRepository.save(todoList)
    user
  }

  def find(identifier: String): Optional[User] = {
    userRepository.find(new UserId(identifier))
  }
}