package com.tradebyte.todoapp.adapter.rest.exceptions

import com.tradebyte.todoapp.domain.exceptions.BadRequestException
import com.tradebyte.todoapp.domain.exceptions.ResourceCannotBeModifiedException
import com.tradebyte.todoapp.domain.exceptions.ResourceNotFoundException
import io.mockk.clearAllMocks
import io.mockk.every
import io.mockk.mockk
import jakarta.servlet.http.HttpServletRequest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.springframework.http.HttpStatus

internal class ProblemJsonExceptionHandlerTest {

    private val underTest = ProblemJsonExceptionHandler()

    @AfterEach
    fun reset() {
        clearAllMocks()
    }

    @Nested
    inner class HandleResourceNotFoundException {

        @Test
        fun `should return 404 response with correct error body`() {
            // Given
            val requestUri = "/api/todos/123"
            val request = mockk<HttpServletRequest>()
            every { request.requestURI } returns requestUri
            val exception = ResourceNotFoundException("Todo item with id 123 not found")

            // When
            val response = underTest.handleResourceNotFoundException(exception, request)

            // Then
            assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
            val body = response.body
            requireNotNull(body)
            assertNotNull(body.timestamp)
            assertEquals(HttpStatus.NOT_FOUND.value(), body.status)
            assertEquals(HttpStatus.NOT_FOUND.reasonPhrase, body.error)
            assertEquals(exception.message, body.message)
            assertEquals(requestUri, body.path)
        }
    }

    @Nested
    inner class HandleBadRequestException {

        @Test
        fun `should return 400 response with correct error body`() {
            // Given
            val requestUri = "/api/todos"
            val request = mockk<HttpServletRequest>()
            every { request.requestURI } returns requestUri
            val exception = BadRequestException("description must not be blank")

            // When
            val response = underTest.handleBadRequestException(exception, request)

            // Then
            assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
            val body = response.body
            requireNotNull(body)
            assertNotNull(body.timestamp)
            assertEquals(HttpStatus.BAD_REQUEST.value(), body.status)
            assertEquals(HttpStatus.BAD_REQUEST.reasonPhrase, body.error)
            assertEquals(exception.message, body.message)
            assertEquals(requestUri, body.path)
        }
    }

    @Nested
    inner class HandleResourceCannotBeModifiedException {

        @Test
        fun `should return 422 response with correct error body`() {
            // Given
            val requestUri = "/api/todos/1"
            val request = mockk<HttpServletRequest>()
            every { request.requestURI } returns requestUri
            val exception = ResourceCannotBeModifiedException("Cannot edit a past due todo item")

            // When
            val response = underTest.handleResourceCannotBeModifiedException(exception, request)

            // Then
            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.statusCode)
            val body = response.body
            requireNotNull(body)
            assertNotNull(body.timestamp)
            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.value(), body.status)
            assertEquals(HttpStatus.UNPROCESSABLE_ENTITY.reasonPhrase, body.error)
            assertEquals(exception.message, body.message)
            assertEquals(requestUri, body.path)
        }
    }
}
