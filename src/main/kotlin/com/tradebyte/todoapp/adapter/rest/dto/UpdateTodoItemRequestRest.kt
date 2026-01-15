package com.tradebyte.todoapp.adapter.rest.dto

import com.tradebyte.todoapp.domain.model.UpdateTodoItemRequest

data class UpdateTodoItemRequestRest(
    val description: String? = null,
    val status: Status? = null,
) {
    fun toRequest(id: Long) = UpdateTodoItemRequest(
        id = id,
        description = this.description,
        status = this.status?.let {
            when (it) {
                Status.DONE -> UpdateTodoItemRequest.Status.DONE
                Status.NOT_DONE -> UpdateTodoItemRequest.Status.NOT_DONE
            }
        }
    )

    enum class Status {
        DONE,
        NOT_DONE,
    }
}
