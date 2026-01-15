package com.tradebyte.todoapp.adapter.rest.dto

import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.time.Instant

data class CreateTodoItemRequestRest(
    @field:NotBlank
    val description: String,
    @field:NotNull
    val dueDateTime: Instant
)
