package com.tradebyte.todoapp.domain.service

import com.tradebyte.todoapp.domain.model.TodoItem
import java.time.Instant

interface TodoRepository {
    fun save(todoItem: TodoItem): TodoItem
    fun findById(id: Long): TodoItem
    fun findAll(): List<TodoItem>
    fun findByStatus(status: TodoItem.Status): List<TodoItem>
    fun findByStatusAndDueDateTimeBefore(status: TodoItem.Status, dueDateTime: Instant): List<TodoItem>
}
