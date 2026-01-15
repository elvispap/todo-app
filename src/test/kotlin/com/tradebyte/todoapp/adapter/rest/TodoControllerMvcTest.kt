package com.tradebyte.todoapp.adapter.rest

import com.tradebyte.todoapp.adapter.rest.exceptions.ProblemJsonExceptionHandler
import com.tradebyte.todoapp.domain.exceptions.ResourceNotFoundException
import com.tradebyte.todoapp.domain.model.TodoItem
import com.tradebyte.todoapp.domain.model.UpdateTodoItemRequest
import com.tradebyte.todoapp.domain.service.TodoService
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
import org.springframework.test.web.servlet.setup.MockMvcBuilders
import java.time.Instant

internal class TodoControllerMvcTest {

    private val todoService: TodoService = mockk()

    private val mockMvc: MockMvc = MockMvcBuilders
        .standaloneSetup(TodoController(todoService))
        .setControllerAdvice(ProblemJsonExceptionHandler())
        .build()

    @Nested
    @DisplayName("POST /api/todos")
    inner class CreateTodoItem {

        @Test
        fun `should create todoItem and return 201`() {
            val due = Instant.parse("2026-01-20T15:30:00Z")
            val created = TodoItem(
                id = 1L,
                description = "Something",
                status = TodoItem.Status.NOT_DONE,
                creationDateTime = Instant.parse("2026-01-13T08:00:00Z"),
                dueDateTime = due,
                doneDateTime = null
            )
            every { todoService.createTodoItem("Something", due) } returns created

            mockMvc.perform(
                post("/api/todos")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(
                        """{
                          "description": "Something",
                          "dueDateTime": "2026-01-20T15:30:00Z"
                        }""".trimIndent()
                    )
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Something"))
                .andExpect(jsonPath("$.status").value("not done"))

            verify(exactly = 1) { todoService.createTodoItem("Something", due) }
        }
    }

    @Nested
    @DisplayName("GET /api/todos")
    inner class GetAllTodoItems {

        @Test
        fun `should return all not done todoItems`() {
            val items = listOf(
                TodoItem(
                    id = 1L,
                    description = "A",
                    status = TodoItem.Status.NOT_DONE,
                    creationDateTime = Instant.parse("2026-01-13T08:00:00Z"),
                    dueDateTime = Instant.parse("2026-01-20T15:30:00Z"),
                    doneDateTime = null
                )
            )
            every { todoService.getAllNotDoneTodoItems() } returns items

            mockMvc.perform(get("/api/todos").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].status").value("not done"))

            verify(exactly = 1) { todoService.getAllNotDoneTodoItems() }
            verify(exactly = 0) { todoService.getAllTodoItems() }
        }
    }

    @Nested
    @DisplayName("GET /api/todos/{id}")
    inner class GetTodoItemById {

        @Test
        fun `should return 404 when provided todoItem cannot be found`() {
            every { todoService.getTodoItemById(123L) } throws ResourceNotFoundException("Todo item with id 123 not found")

            mockMvc.perform(get("/api/todos/123").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Todo item with id 123 not found"))
                .andExpect(jsonPath("$.path").value("/api/todos/123"))

            verify(exactly = 1) { todoService.getTodoItemById(123L) }
        }
    }

    @Nested
    @DisplayName("PATCH /api/todos/{id}")
    inner class UpdateTodoItem {

        @Test
        fun `should update todoItem and return 200 when valid request is provided`() {
            val updated = TodoItem(
                id = 1L,
                description = "Updated",
                status = TodoItem.Status.DONE,
                creationDateTime = Instant.parse("2026-01-13T08:00:00Z"),
                dueDateTime = Instant.parse("2026-01-20T15:30:00Z"),
                doneDateTime = Instant.parse("2026-01-14T10:00:00Z")
            )

            every {
                todoService.updateTodoItem(
                    UpdateTodoItemRequest(
                        id = 1L,
                        description = "Updated",
                        status = UpdateTodoItemRequest.Status.DONE
                    )
                )
            } returns updated

            mockMvc.perform(
                patch("/api/todos/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content(
                        """{
                          "description": "Updated",
                          "status": "DONE"
                        }""".trimIndent()
                    )
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.description").value("Updated"))
                .andExpect(jsonPath("$.status").value("done"))
                .andExpect(jsonPath("$.doneDateTime").value("2026-01-14T10:00:00Z"))

            verify(exactly = 1) {
                todoService.updateTodoItem(
                    UpdateTodoItemRequest(
                        id = 1L,
                        description = "Updated",
                        status = UpdateTodoItemRequest.Status.DONE
                    )
                )
            }
        }

        @Test
        fun `should return 400 when request is invalid`() {
            mockMvc.perform(
                patch("/api/todos/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .accept(MediaType.APPLICATION_JSON)
                    .content("""{}""")
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.error").value("Bad Request"))
                .andExpect(jsonPath("$.message").value("At least one field (description or status) must be provided for update."))
                .andExpect(jsonPath("$.path").value("/api/todos/1"))

            verify(exactly = 0) {
                todoService.updateTodoItem(any())
            }
        }
    }
}
