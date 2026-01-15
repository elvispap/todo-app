package com.tradebyte.todoapp.application.scheduling

import com.tradebyte.todoapp.domain.service.TodoService
import io.mockk.mockk
import io.mockk.verify
import org.junit.jupiter.api.Test

internal class TodoItemsStatusSchedulerTest {

    private val todoService: TodoService = mockk(relaxed = true)
    private val underTest = TodoItemsStatusScheduler(todoService)

    @Test
    fun `run should trigger overdue todo items status update`() {
        // When
        underTest.run()

        // Then
        verify(exactly = 1) { todoService.checkForOverdueTodoItemsStatusUpdate() }
    }
}
