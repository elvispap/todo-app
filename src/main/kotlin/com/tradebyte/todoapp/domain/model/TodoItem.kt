package com.tradebyte.todoapp.domain.model

import java.time.Instant

data class TodoItem(
    val id: Long? = null,
    val description: String,
    val status: Status,
    val creationDateTime: Instant,
    val dueDateTime: Instant,
    val doneDateTime: Instant? = null,
) {

    fun isNotDone(): Boolean = status == Status.NOT_DONE

    enum class Status(val value: String) {
        NOT_DONE("not done"),
        DONE("done"),
        PAST_DUE("past due")
    }
}
