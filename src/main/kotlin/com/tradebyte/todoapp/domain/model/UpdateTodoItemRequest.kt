package com.tradebyte.todoapp.domain.model

data class UpdateTodoItemRequest(
    val id: Long,
    val description: String? = null,
    val status: Status? = null,
) {
    enum class Status {
        DONE,
        NOT_DONE,
    }
}
