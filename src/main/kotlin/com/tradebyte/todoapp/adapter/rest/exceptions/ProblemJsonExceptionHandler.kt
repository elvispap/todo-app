package com.tradebyte.todoapp.adapter.rest.exceptions

import com.tradebyte.todoapp.application.utils.logger
import com.tradebyte.todoapp.domain.exceptions.BadRequestException
import com.tradebyte.todoapp.domain.exceptions.ResourceCannotBeModifiedException
import com.tradebyte.todoapp.domain.exceptions.ResourceNotFoundException
import jakarta.servlet.http.HttpServletRequest
import org.springframework.boot.logging.LogLevel
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.ResponseBody
import java.time.Instant

internal data class ErrorResponse(
    val timestamp: String = Instant.now().toString(),
    val status: Int,
    val error: String,
    val message: String?,
    val path: String
)

@ControllerAdvice
internal class ProblemJsonExceptionHandler {

    private val log = logger()

    @ExceptionHandler(value = [ResourceNotFoundException::class])
    @ResponseBody
    fun handleResourceNotFoundException(
        exception: ResourceNotFoundException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.NOT_FOUND.value(),
            error = HttpStatus.NOT_FOUND.reasonPhrase,
            message = exception.message,
            path = request.requestURI
        )
        logResponseProblem(exception, HttpStatus.NOT_FOUND.value())
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse)
    }

    @ExceptionHandler(value = [BadRequestException::class])
    @ResponseBody
    fun handleBadRequestException(
        exception: BadRequestException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.BAD_REQUEST.value(),
            error = HttpStatus.BAD_REQUEST.reasonPhrase,
            message = exception.message,
            path = request.requestURI
        )
        logResponseProblem(exception, HttpStatus.BAD_REQUEST.value())
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse)
    }

    @ExceptionHandler(value = [ResourceCannotBeModifiedException::class])
    @ResponseBody
    fun handleResourceCannotBeModifiedException(
        exception: ResourceCannotBeModifiedException,
        request: HttpServletRequest
    ): ResponseEntity<ErrorResponse> {
        val errorResponse = ErrorResponse(
            status = HttpStatus.UNPROCESSABLE_ENTITY.value(),
            error = HttpStatus.UNPROCESSABLE_ENTITY.reasonPhrase,
            message = exception.message,
            path = request.requestURI
        )
        logResponseProblem(exception, HttpStatus.UNPROCESSABLE_ENTITY.value())
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(errorResponse)
    }

    private fun logResponseProblem(
        exception: Throwable,
        statusCode: Int,
        deviatingLogLevel: LogLevel? = null,
    ) {
        val logMessage = buildLogMessage(statusCode, exception)

        val logLevel = deviatingLogLevel ?: when (statusCode) {
            in 400..499 -> LogLevel.INFO
            in 500..599 -> LogLevel.ERROR
            else -> LogLevel.ERROR
        }
        when (logLevel) {
            LogLevel.DEBUG -> log.debug(logMessage, exception)
            LogLevel.INFO -> log.info(logMessage, exception)
            LogLevel.WARN -> log.warn(logMessage, exception)
            LogLevel.ERROR -> log.error(logMessage, exception)
            else -> log.error(logMessage, exception)
        }
    }

    private fun buildLogMessage(statusCode: Int?, exception: Throwable): String {
        val errorCategorization = when (statusCode) {
            in 400..499 -> "Client error during request"
            in 500..599 -> "Server error during request"
            else -> "Error during request"
        }
        val exceptionMessage = exception.message ?: "No Exception message available"

        return "$errorCategorization: $exceptionMessage"
    }
}
