package com.codefactrory.shorty.infrastructure.adapter.incoming

import com.codefactrory.shorty.domain.port.UrlRepositoryPortError
import com.codefactrory.shorty.domain.port.UrlRepositoryPortError.UrlRepositoryPortNotFoundError
import com.codefactrory.shorty.domain.port.UrlRepositoryPortError.UrlRepositoryPortUnexpectedError
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
    ): ResponseEntity<String> {
        val errors = e.bindingResult.fieldErrors.joinToString(", ") { fieldError ->
            "${fieldError.field}: ${fieldError.defaultMessage}"
        }

        return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(errors)
    }

    @ExceptionHandler(UrlRepositoryPortNotFoundError::class)
    fun handleNotFoundException(
        e: UrlRepositoryPortNotFoundError
    ): ResponseEntity<String>{
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(e.message)
    }
    
    @ExceptionHandler(IllegalArgumentException::class)
    fun handleIllegalArgumentException(
        e: IllegalArgumentException
    ): ResponseEntity<String> {
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.message ?: "Invalid input")
    }
}