package com.tradebyte.todoapp.adapter.persistence

import com.tradebyte.todoapp.domain.model.TodoItem
import jakarta.persistence.Entity
import jakarta.persistence.GeneratedValue
import jakarta.persistence.GenerationType
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.Instant

@Entity
@Table(name = "todo_item")
data class TodoItemEntity(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = 0,
    val description: String,
    val status: StatusDB,
    val creationDateTime: Instant,
    val dueDateTime: Instant,
    val doneDateTime: Instant? = null,
) {
    enum class StatusDB(val value: String) {
        NOT_DONE("not done"),
        DONE("done"),
        PAST_DUE("past due");

        fun toStatus(): TodoItem.Status = when (this) {
            NOT_DONE -> TodoItem.Status.NOT_DONE
            DONE -> TodoItem.Status.DONE
            PAST_DUE -> TodoItem.Status.PAST_DUE
        }
    }

    fun toTodoItem() = TodoItem(
        id = this.id,
        description = this.description,
        status = this.status.toStatus(),
        creationDateTime = this.creationDateTime,
        dueDateTime = this.dueDateTime,
        doneDateTime = this.doneDateTime
    )
}

fun TodoItem.toTodoItemEntity() = TodoItemEntity(
    id = this.id,
    description = this.description,
    status = this.status.toStatusDB(),
    creationDateTime = this.creationDateTime,
    dueDateTime = this.dueDateTime,
    doneDateTime = this.doneDateTime
)

fun TodoItem.Status.toStatusDB() = when (this) {
    TodoItem.Status.NOT_DONE -> TodoItemEntity.StatusDB.NOT_DONE
    TodoItem.Status.DONE -> TodoItemEntity.StatusDB.DONE
    TodoItem.Status.PAST_DUE -> TodoItemEntity.StatusDB.PAST_DUE
}
