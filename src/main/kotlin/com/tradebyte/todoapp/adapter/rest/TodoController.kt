package com.tradebyte.todoapp.adapter.rest

import com.tradebyte.todoapp.adapter.rest.dto.CreateTodoItemRequestRest
import com.tradebyte.todoapp.adapter.rest.dto.TodoItemRest
import com.tradebyte.todoapp.adapter.rest.dto.UpdateTodoItemRequestRest
import com.tradebyte.todoapp.adapter.rest.dto.toTodoItemRest
import com.tradebyte.todoapp.domain.exceptions.BadRequestException
import com.tradebyte.todoapp.domain.service.TodoService
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.validation.annotation.Validated
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@Validated
@RequestMapping("api/todos", produces = [MediaType.APPLICATION_JSON_VALUE])
internal class TodoController(
    private val todoService: TodoService
) {

    @PostMapping
    fun createTodoItem(
        @Valid @RequestBody request: CreateTodoItemRequestRest
    ): ResponseEntity<TodoItemRest> {
        val todoItem = todoService.createTodoItem(
            description = request.description,
            dueDateTime = request.dueDateTime
        )

        val response = todoItem.toTodoItemRest()
        return ResponseEntity.status(HttpStatus.CREATED).body(response)
    }

    @GetMapping
    fun getAllTodoItems(
        @RequestParam(name = "includeAll", defaultValue = "false") includeAll: Boolean
    ): ResponseEntity<List<TodoItemRest>> {
        val todoItems = if (includeAll) {
            todoService.getAllTodoItems()
        } else {
            todoService.getAllNotDoneTodoItems()
        }

        val response = todoItems.map { it.toTodoItemRest() }
        return ResponseEntity.ok(response)
    }

    @GetMapping("/{id}")
    fun getTodoItemById(
        @PathVariable id: Long
    ): ResponseEntity<TodoItemRest> {
        val todoItem = todoService.getTodoItemById(id)

        val response = todoItem.toTodoItemRest()
        return ResponseEntity.ok(response)
    }

    @PatchMapping("/{id}")
    fun updateTodoItem(
        @PathVariable id: Long,
        @Valid @RequestBody request: UpdateTodoItemRequestRest
    ): ResponseEntity<TodoItemRest> {
        if (request.description == null && request.status == null) {
            throw BadRequestException("At least one field (description or status) must be provided for update.")
        }
        val todoItem = todoService.updateTodoItem(
            request = request.toRequest(id)
        )

        val response = todoItem.toTodoItemRest()
        return ResponseEntity.ok(response)
    }
}
