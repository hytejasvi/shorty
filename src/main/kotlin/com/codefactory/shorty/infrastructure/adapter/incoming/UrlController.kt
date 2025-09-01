package com.codefactory.shorty.infrastructure.adapter.incoming

import com.codefactory.shorty.application.service.UrlService
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
    val urlService: UrlService
) {

    @PostMapping("/shorten")
    fun getShortnedUrl(
        @Valid
        @RequestBody urlRequestDto: UrlRequestDto
    ): ResponseEntity<UrlResponseDto> {
        val response = urlService.createShortUrl(urlRequestDto)
        return ResponseEntity
            .created(URI.create(response.shortUrl))
            .body(response)
    }

    @GetMapping("/{shortUrlCode}")
    fun redirectToOriginalUrl(
        @PathVariable shortUrlCode: String
    ): ResponseEntity<Void> {
        val originalUrl = urlService.getOriginalUrl(shortUrlCode)

        return ResponseEntity.status(HttpStatus.FOUND)
            .location(URI.create(originalUrl))
            .build()
    }

    @GetMapping("/original/{shortUrlCode}")
    fun getOriginalUrl(
        @PathVariable shortUrlCode: String
    ): ResponseEntity<String> {
        val originalUrl = urlService.getOriginalUrl(shortUrlCode)
        return ResponseEntity.status(HttpStatus.OK).body(originalUrl)
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