package com.codefactory.shorty.infrastructure.adapter.incoming

import com.codefactory.shorty.application.service.UrlService
import com.codefactory.shorty.application.service.UrlServiceError
import com.codefactory.shorty.application.service.UrlServiceError.UrlServiceInvalidInputError
import com.codefactory.shorty.application.service.UrlServiceError.UrlServiceNotFoundError
import com.codefactory.shorty.application.service.UrlServiceError.UrlServiceUnexpectedError
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.URL
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController
import java.net.URI

@RestController
class UrlController(
    private val urlService: UrlService
) {

    @PostMapping("/shorten")
    fun createShortUrl(
        @Valid @RequestBody urlRequestDto: UrlRequestDto
    ): ResponseEntity<*> =
        urlService.createShortUrl(urlRequestDto).fold(
            ifLeft = { error -> toResponse(error) },
            ifRight = { response ->
                ResponseEntity
                    .created(URI.create(response.shortUrl))
                    .body(response)
            }
        )

    @GetMapping("/{shortUrlCode}")
    fun redirectToOriginalUrl(
        @PathVariable shortUrlCode: String
    ): ResponseEntity<*> =
        urlService.getOriginalUrl(shortUrlCode).fold(
            ifLeft = { error -> toResponse(error) },
            ifRight = { response ->
                ResponseEntity.status(HttpStatus.FOUND)
                    .location(URI.create(response.originalUrl))
                    .build<Unit>()
            }
        )

    @GetMapping("/original/{shortUrlCode}")
    fun getOriginalUrl(
        @PathVariable shortUrlCode: String
    ): ResponseEntity<*> =
        urlService.getOriginalUrl(shortUrlCode).fold(
            ifLeft = { error -> toResponse(error) },
            ifRight = { response -> ResponseEntity.ok(response) }
        )

    private fun toResponse(error: UrlServiceError): ResponseEntity<ErrorResponse> =
        when (error) {
            is UrlServiceInvalidInputError ->
                ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(ErrorResponse(HttpStatus.BAD_REQUEST.value(), error.message))

            is UrlServiceNotFoundError ->
                ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ErrorResponse(HttpStatus.NOT_FOUND.value(), error.message))

            is UrlServiceUnexpectedError ->
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(ErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), error.message))
        }
}


data class UrlRequestDto(
    @field:NotBlank(message= "Url should not be blank")
    val originalUrl: String
)

data class UrlResponseDto(
    val originalUrl: String,
    val shortUrl: String,
)