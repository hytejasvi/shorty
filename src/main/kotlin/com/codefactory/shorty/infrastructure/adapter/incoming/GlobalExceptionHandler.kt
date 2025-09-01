package com.codefactory.shorty.infrastructure.adapter.incoming

import com.codefactory.shorty.domain.port.UrlRepositoryPortError.UrlRepositoryPortNotFoundError
import com.codefactory.shorty.domain.port.UrlRepositoryPortError.UrlRepositoryPortUnexpectedError
import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.servlet.http.HttpServletRequest
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidationException(
        e: MethodArgumentNotValidException
    ): ResponseEntity<ErrorResponse> {
        val errors = e.bindingResult
            .fieldErrors
            .joinToString(", ")
            {
                it.defaultMessage ?: "Validation error"
            }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(HttpStatus.BAD_REQUEST.value(), errors))
    }

    @ExceptionHandler(UrlRepositoryPortNotFoundError::class)
    fun handleNotFoundError(
        e: UrlRepositoryPortNotFoundError,
        request: HttpServletRequest,
    ): ResponseEntity<ErrorResponse> {
        val message = when {
            request.requestURI.startsWith("/original/") ->
                e.message
            else ->
                "Short code not found"
        }

        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(ErrorResponse(HttpStatus.NOT_FOUND.value(), message))
    }
    
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        e: IllegalArgumentException
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.BAD_REQUEST)
            .body(ErrorResponse(HttpStatus.BAD_REQUEST.value(), e.message))

    @ExceptionHandler(UrlRepositoryPortUnexpectedError::class)
    fun handleUnexpectedError(
        e: UrlRepositoryPortUnexpectedError
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.message))

    @ExceptionHandler(Exception::class)
    fun handleGenericException(
        e: Exception
    ): ResponseEntity<ErrorResponse> =
        ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .body(ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), e.message))
}

data class ErrorResponse(
    @JsonProperty("status")
    val status: Int,

    @JsonProperty("message")
    val message: String?,
)