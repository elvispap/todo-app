package com.tradebyte.todoapp.domain.service

import com.tradebyte.todoapp.domain.exceptions.ResourceCannotBeModifiedException
import com.tradebyte.todoapp.domain.exceptions.ResourceNotFoundException
import com.tradebyte.todoapp.domain.model.TodoItem
import com.tradebyte.todoapp.domain.model.UpdateTodoItemRequest
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.time.Instant
import java.time.temporal.ChronoUnit

internal class TodoServiceTest {

    private val todoRepository: TodoRepository = mockk()
    private val underTest: TodoService = TodoService(todoRepository)

    @BeforeEach
    fun setup() {
        clearAllMocks()
    }

    @Nested
    inner class CreateTodoItem {

        @Test
        fun `should create new todoItem`() {
            // Given
            val description = "Test TODO"
            val dueDateTime = Instant.now().plus(1, ChronoUnit.DAYS)
            val savedTodoItem = createTestTodoItem(
                description = description,
                status = TodoItem.Status.NOT_DONE,
                dueDateTime = dueDateTime
            )

            every { todoRepository.save(any()) } returns savedTodoItem

            // When
            val result = underTest.createTodoItem(description, dueDateTime)

            // Then
            assertNotNull(result)
            assertEquals(description, result.description)
            assertEquals(TodoItem.Status.NOT_DONE, result.status)
            assertEquals(dueDateTime, result.dueDateTime)
            assertNull(result.doneDateTime)

            verify(exactly = 1) { todoRepository.save(any()) }
        }

        @Test
        fun `should create new todo with status PAST_DUE when provided dueDateTime is in the past`() {
            // Given
            val description = "Test TODO"
            val dueDateTime = Instant.now().minus(1, ChronoUnit.DAYS)

            val savedTodoSlot = slot<TodoItem>()
            every { todoRepository.save(capture(savedTodoSlot)) } answers { savedTodoSlot.captured }

            // When
            val result = underTest.createTodoItem(description, dueDateTime)

            // Then
            assertNotNull(result)
            assertEquals(description, result.description)
            assertEquals(TodoItem.Status.PAST_DUE, result.status)
            assertEquals(dueDateTime, result.dueDateTime)
            assertNull(result.doneDateTime)

            assertEquals(description, savedTodoSlot.captured.description)
            assertEquals(TodoItem.Status.PAST_DUE, savedTodoSlot.captured.status)
            assertEquals(dueDateTime, savedTodoSlot.captured.dueDateTime)

            verify(exactly = 1) { todoRepository.save(any()) }
        }
    }

    @Nested
    inner class GetTodoItemById {

        @Test
        fun `should return todoItem when exists`() {
            // Given
            val todoId = 1L
            val expectedTodo = createTestTodoItem(id = todoId)
            every { todoRepository.findById(todoId) } returns expectedTodo

            // When
            val result = underTest.getTodoItemById(todoId)

            // Then
            assertEquals(expectedTodo, result)
            verify(exactly = 1) { todoRepository.findById(todoId) }
        }

        @Test
        fun `should update status to PAST_DUE and persist when todoItem is overdue`() {
            // Given
            val todoId = 1L
            val pastDueDateTime = Instant.now().minus(1, ChronoUnit.DAYS)
            val existingTodo = createTestTodoItem(
                id = todoId,
                status = TodoItem.Status.NOT_DONE,
                dueDateTime = pastDueDateTime
            )

            val savedTodoSlot = slot<TodoItem>()
            every { todoRepository.findById(todoId) } returns existingTodo
            every { todoRepository.save(capture(savedTodoSlot)) } answers { savedTodoSlot.captured }

            // When
            val result = underTest.getTodoItemById(todoId)

            // Then
            assertEquals(todoId, result.id)
            assertEquals(TodoItem.Status.PAST_DUE, result.status)

            assertEquals(todoId, savedTodoSlot.captured.id)
            assertEquals(TodoItem.Status.PAST_DUE, savedTodoSlot.captured.status)
            assertEquals(existingTodo.description, savedTodoSlot.captured.description)
            assertEquals(existingTodo.dueDateTime, savedTodoSlot.captured.dueDateTime)

            verify(exactly = 1) { todoRepository.findById(todoId) }
            verify(exactly = 1) { todoRepository.save(any()) }
        }

        @Test
        fun `should not update status when todoItem is not overdue`() {
            // Given
            val todoId = 1L
            val futureDueDateTime = Instant.now().plus(1, ChronoUnit.DAYS)
            val existingTodo = createTestTodoItem(
                id = todoId,
                status = TodoItem.Status.NOT_DONE,
                dueDateTime = futureDueDateTime
            )

            every { todoRepository.findById(todoId) } returns existingTodo

            // When
            val result = underTest.getTodoItemById(todoId)

            // Then
            assertEquals(todoId, result.id)
            assertEquals(TodoItem.Status.NOT_DONE, result.status)

            verify(exactly = 1) { todoRepository.findById(todoId) }
            verify(exactly = 0) { todoRepository.save(any()) }
        }

        @Test
        fun `should throw exception when todoItem is not found`() {
            // Given
            val todoId = 999L
            every {
                todoRepository.findById(todoId)
            } throws ResourceNotFoundException("Todo item with id $todoId not found")

            // When & Then
            assertThrows<ResourceNotFoundException> {
                underTest.getTodoItemById(todoId)
            }
        }
    }

    @Nested
    inner class GetAllTodoItems {

        @Test
        fun `should return only NOT_DONE items`() {
            // Given
            val notDoneItems = listOf(
                createTestTodoItem(id = 1L, status = TodoItem.Status.NOT_DONE),
                createTestTodoItem(id = 2L, status = TodoItem.Status.NOT_DONE)
            )
            every { todoRepository.findByStatus(TodoItem.Status.NOT_DONE) } returns notDoneItems

            // When
            val result = underTest.getAllNotDoneTodoItems()

            // Then
            assertEquals(2, result.size)
            assertTrue(result.all { it.status == TodoItem.Status.NOT_DONE })

            verify(exactly = 1) { todoRepository.findByStatus(TodoItem.Status.NOT_DONE) }
        }

        @Test
        fun `should return all items`() {
            // Given
            val allItems = listOf(
                createTestTodoItem(id = 1L, status = TodoItem.Status.NOT_DONE),
                createTestTodoItem(id = 2L, status = TodoItem.Status.DONE),
                createTestTodoItem(id = 3L, status = TodoItem.Status.PAST_DUE)
            )
            every { todoRepository.findAll() } returns allItems

            // When
            val result = underTest.getAllTodoItems()

            // Then
            assertEquals(3, result.size)
            verify(exactly = 1) { todoRepository.findAll() }
        }
    }

    @Nested
    inner class UpdateTodoItem {

        @Test
        fun `should update description when provided`() {
            // Given
            val todoId = 1L
            val originalTodo = createTestTodoItem(id = todoId, description = "Original description")
            val newDescription = "Updated description"
            val request = UpdateTodoItemRequest(id = todoId, description = newDescription)

            val savedTodoSlot = slot<TodoItem>()
            every { todoRepository.findById(todoId) } returns originalTodo
            every { todoRepository.save(capture(savedTodoSlot)) } answers { savedTodoSlot.captured }

            // When
            val result = underTest.updateTodoItem(request)

            // Then
            assertEquals(newDescription, result.description)
            assertEquals(originalTodo.status, result.status)
            verify(exactly = 1) { todoRepository.save(any()) }
        }

        @Test
        fun `should update status to DONE when provided`() {
            // Given
            val todoId = 1L
            val originalTodo = createTestTodoItem(id = todoId, status = TodoItem.Status.NOT_DONE)
            val request = UpdateTodoItemRequest(
                id = todoId,
                status = UpdateTodoItemRequest.Status.DONE
            )

            val savedTodoSlot = slot<TodoItem>()
            every { todoRepository.findById(todoId) } returns originalTodo
            every { todoRepository.save(capture(savedTodoSlot)) } answers { savedTodoSlot.captured }

            // When
            val result = underTest.updateTodoItem(request)

            // Then
            assertEquals(TodoItem.Status.DONE, result.status)
            assertNotNull(result.doneDateTime)
            verify(exactly = 1) { todoRepository.save(any()) }
        }

        @Test
        fun `should update status to NOT_DONE and clear doneDateTime`() {
            // Given
            val todoId = 1L
            val originalTodo = createTestTodoItem(
                id = todoId,
                status = TodoItem.Status.DONE,
                doneDateTime = Instant.now()
            )
            val request = UpdateTodoItemRequest(
                id = todoId,
                status = UpdateTodoItemRequest.Status.NOT_DONE
            )

            val savedTodoSlot = slot<TodoItem>()
            every { todoRepository.findById(todoId) } returns originalTodo
            every { todoRepository.save(capture(savedTodoSlot)) } answers { savedTodoSlot.captured }

            // When
            val result = underTest.updateTodoItem(request)

            // Then
            assertEquals(TodoItem.Status.NOT_DONE, result.status)
            assertNull(result.doneDateTime)
            verify(exactly = 1) { todoRepository.save(any()) }
        }

        @Test
        fun `should update both description and status`() {
            // Given
            val todoId = 1L
            val originalTodo = createTestTodoItem(
                id = todoId,
                description = "Old description",
                status = TodoItem.Status.NOT_DONE
            )
            val request = UpdateTodoItemRequest(
                id = todoId,
                description = "New description",
                status = UpdateTodoItemRequest.Status.DONE
            )

            val savedTodoSlot = slot<TodoItem>()
            every { todoRepository.findById(todoId) } returns originalTodo
            every { todoRepository.save(capture(savedTodoSlot)) } answers { savedTodoSlot.captured }

            // When
            val result = underTest.updateTodoItem(request)

            // Then
            assertEquals("New description", result.description)
            assertEquals(TodoItem.Status.DONE, result.status)
            assertNotNull(result.doneDateTime)
            verify(exactly = 1) { todoRepository.save(any()) }
        }

        @Test
        fun `should throw exception when todo is PAST_DUE`() {
            // Given
            val todoId = 1L
            val pastDueTodo = createTestTodoItem(id = todoId, status = TodoItem.Status.PAST_DUE)
            val request = UpdateTodoItemRequest(id = todoId, description = "Try to update")

            every { todoRepository.findById(todoId) } returns pastDueTodo

            // When & Then
            val exception = assertThrows<ResourceCannotBeModifiedException> {
                underTest.updateTodoItem(request)
            }
            assertTrue(exception.message!!.contains("Cannot edit a past due todo item"))
            verify(exactly = 0) { todoRepository.save(any()) }
        }

        @Test
        fun `should keep existing values when not provided in request`() {
            // Given
            val todoId = 1L
            val originalTodo = createTestTodoItem(
                id = todoId,
                description = "Original description",
                status = TodoItem.Status.NOT_DONE
            )
            val request = UpdateTodoItemRequest(id = todoId) // No description or status

            val savedTodoSlot = slot<TodoItem>()
            every { todoRepository.findById(todoId) } returns originalTodo
            every { todoRepository.save(capture(savedTodoSlot)) } answers { savedTodoSlot.captured }

            // When
            val result = underTest.updateTodoItem(request)

            // Then
            assertEquals(originalTodo.description, result.description)
            assertEquals(originalTodo.status, result.status)
            assertEquals(originalTodo.doneDateTime, result.doneDateTime)
        }
    }

    @Nested
    inner class CheckForOverdueTodoItemsStatusUpdate {

        @Test
        fun `should mark overdue items as PAST_DUE`() {
            // Given
            val now = Instant.now()
            val pastDueDate = now.minus(1, ChronoUnit.DAYS)
            val overdueItems = listOf(
                createTestTodoItem(id = 1L, dueDateTime = pastDueDate),
                createTestTodoItem(id = 2L, dueDateTime = pastDueDate)
            )

            val savedItems = mutableListOf<TodoItem>()
            every {
                todoRepository.findByStatusAndDueDateTimeBefore(
                    TodoItem.Status.NOT_DONE,
                    any()
                )
            } returns overdueItems
            every { todoRepository.save(any()) } answers {
                val item = it.invocation.args[0] as TodoItem
                savedItems.add(item)
                item
            }

            // When
            underTest.checkForOverdueTodoItemsStatusUpdate()

            // Then
            assertEquals(2, savedItems.size)
            assertTrue(savedItems.all { it.status == TodoItem.Status.PAST_DUE })
            verify(exactly = 2) { todoRepository.save(any()) }
        }

        @Test
        fun `should not mark future items as PAST_DUE`() {
            // Given
            every {
                todoRepository.findByStatusAndDueDateTimeBefore(
                    TodoItem.Status.NOT_DONE,
                    any()
                )
            } returns emptyList()

            // When
            underTest.checkForOverdueTodoItemsStatusUpdate()

            // Then
            verify(exactly = 0) { todoRepository.save(any()) }
        }
    }

    // Helper method to create test TodoItem
    private fun createTestTodoItem(
        id: Long = 1L,
        description: String = "Test TODO",
        status: TodoItem.Status = TodoItem.Status.NOT_DONE,
        dueDateTime: Instant = Instant.now().plus(1, ChronoUnit.DAYS),
        doneDateTime: Instant? = null
    ): TodoItem = TodoItem(
        id = id,
        description = description,
        status = status,
        creationDateTime = Instant.now(),
        dueDateTime = dueDateTime,
        doneDateTime = doneDateTime
    )
}
