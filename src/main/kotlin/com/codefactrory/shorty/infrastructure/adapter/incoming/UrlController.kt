package com.codefactrory.shorty.infrastructure.adapter.incoming

import com.codefactrory.shorty.domain.service.UrlService
import jakarta.validation.Valid
import jakarta.validation.constraints.NotBlank
import org.hibernate.validator.constraints.URL
import org.springframework.http.ResponseEntity
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
}

data class UrlRequestDto(
    @field:NotBlank(message= "Url should not be blank")
    val originalUrl: String
)

data class UrlResponseDto(
    val originalUrl: String,
    val shortUrl: String,
)