package com.tradebyte.todoapp.adapter.rest.dto

import com.tradebyte.todoapp.domain.model.TodoItem
import java.time.Instant

data class TodoItemRest(
    val id: Long?,
    val description: String,
    val status: String,
    val creationDateTime: Instant,
    val dueDateTime: Instant,
    val doneDateTime: Instant?
)

fun TodoItem.toTodoItemRest(): TodoItemRest = TodoItemRest(
    id = this.id,
    description = this.description,
    status = this.status.value,
    creationDateTime = this.creationDateTime,
    dueDateTime = this.dueDateTime,
    doneDateTime = this.doneDateTime
)
