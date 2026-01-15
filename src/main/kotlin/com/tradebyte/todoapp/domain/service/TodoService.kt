package com.tradebyte.todoapp.domain.service

import com.tradebyte.todoapp.application.utils.logger
import com.tradebyte.todoapp.domain.exceptions.ResourceCannotBeModifiedException
import com.tradebyte.todoapp.domain.model.TodoItem
import com.tradebyte.todoapp.domain.model.UpdateTodoItemRequest
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.Instant

@Service
class TodoService(
    private val todoRepository: TodoRepository
) {

    private val log = logger()

    @Transactional
    fun createTodoItem(
        description: String,
        dueDateTime: Instant
    ): TodoItem {
        val now = Instant.now()
        val todoItem = TodoItem(
            description = description,
            status = if (!dueDateTime.isAfter(now)) TodoItem.Status.PAST_DUE else TodoItem.Status.NOT_DONE,
            creationDateTime = now,
            dueDateTime = dueDateTime
        )
        return todoRepository.save(todoItem).also { log.debug("Created new todoItem with ID '${it.id}'") }
    }

    fun getTodoItemById(
        id: Long
    ): TodoItem = todoRepository.findById(id).withUpdatedStatus()

    fun getAllNotDoneTodoItems(): List<TodoItem> = todoRepository.findByStatus(TodoItem.Status.NOT_DONE)

    fun getAllTodoItems(): List<TodoItem> = todoRepository.findAll().map { it.withUpdatedStatus() }

    @Transactional
    fun updateTodoItem(
        request: UpdateTodoItemRequest
    ): TodoItem {
        var todoItem = getTodoItemById(request.id)

        checkIfItemCanBeEdited(todoItem)

        request.description?.let {
            todoItem = todoItem.copy(description = request.description)
        }
        request.status?.let {
            todoItem = when (request.status) {
                UpdateTodoItemRequest.Status.DONE -> {
                    todoItem.copy(
                        status = TodoItem.Status.DONE,
                        doneDateTime = Instant.now()
                    )
                }

                UpdateTodoItemRequest.Status.NOT_DONE -> {
                    todoItem.copy(
                        status = TodoItem.Status.NOT_DONE,
                        doneDateTime = null
                    )
                }
            }
        }
        return todoRepository.save(todoItem).also { log.debug("Updated new todoItem with ID '${it.id}'") }
    }

    @Transactional
    fun checkForOverdueTodoItemsStatusUpdate() {
        val now = Instant.now()
        val overdueItems = todoRepository.findByStatusAndDueDateTimeBefore(
            status = TodoItem.Status.NOT_DONE,
            dueDateTime = now.plusMillis(1)
        )
        overdueItems.forEach { todoItem ->
            val updatedTodoItem = todoItem.copy(status = TodoItem.Status.PAST_DUE)
            todoRepository.save(updatedTodoItem)
                .also { log.debug("Marked overdue todoItem with ID `${it.id}` as PAST_DUE") }
        }
    }

    private fun checkIfItemCanBeEdited(
        todoItem: TodoItem
    ) {
        if (todoItem.status == TodoItem.Status.PAST_DUE) {
            throw ResourceCannotBeModifiedException("Cannot edit a past due todo item with id '${todoItem.id}'")
        }
    }

    private fun TodoItem.withUpdatedStatus(): TodoItem {
        if (this.isNotDone() && !dueDateTime.isAfter(Instant.now())) {
            val updatedItem = this.copy(status = TodoItem.Status.PAST_DUE)
            return todoRepository.save(updatedItem)
                .also { log.debug("Marked todoItem with ID `${it.id}` as PAST_DUE") }
        }
        return this
    }
}
