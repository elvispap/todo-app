package com.tradebyte.todoapp.adapter.persistence

import com.tradebyte.todoapp.domain.exceptions.ResourceNotFoundException
import com.tradebyte.todoapp.domain.model.TodoItem
import com.tradebyte.todoapp.domain.service.TodoRepository
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.time.Instant

interface TodoDBInternalRepository : JpaRepository<TodoItemEntity, Long> {
    fun findByStatus(status: TodoItemEntity.StatusDB): List<TodoItemEntity>
    fun findByStatusAndDueDateTimeBefore(status: TodoItemEntity.StatusDB, dueDateTime: Instant): List<TodoItemEntity>
}

@Repository
class TodoDBRepository(
    private val internalRepository: TodoDBInternalRepository
) : TodoRepository {

    override fun save(
        todoItem: TodoItem
    ): TodoItem {
        val entity = todoItem.toTodoItemEntity()
        val savedEntity = internalRepository.save(entity)
        return savedEntity.toTodoItem()
    }

    override fun findById(
        id: Long
    ): TodoItem {
        val entity = internalRepository.findById(id).orElse(null)
            ?: throw ResourceNotFoundException("Todo item with id $id not found")
        return entity.toTodoItem()
    }

    override fun findAll(): List<TodoItem> {
        return internalRepository.findAll().map { it.toTodoItem() }
    }

    override fun findByStatus(
        status: TodoItem.Status
    ): List<TodoItem> {
        val dbStatus = status.toStatusDB()
        return internalRepository.findByStatus(dbStatus).map { it.toTodoItem() }
    }

    override fun findByStatusAndDueDateTimeBefore(
        status: TodoItem.Status,
        dueDateTime: Instant
    ): List<TodoItem> {
        val dbStatus = status.toStatusDB()
        return internalRepository.findByStatusAndDueDateTimeBefore(dbStatus, dueDateTime).map { it.toTodoItem() }
    }
}
