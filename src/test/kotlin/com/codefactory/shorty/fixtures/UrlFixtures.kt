package com.codefactory.shorty.fixtures

import com.codefactory.shorty.infrastructure.adapter.incoming.UrlRequestDto
import com.codefactory.shorty.infrastructure.adapter.incoming.UrlResponseDto
import com.codefactory.shorty.infrastructure.adapter.incoming.ErrorResponse
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.http.ResponseEntity

const val DEFAULT_VALID_URL = "https://www.dkbcodefactory.com/"
const val DEFAULT_VALID_URL_NO_PROTOCOL = "www.example.com"
const val DEFAULT_SHORT_URL = "http://localhost:8080/abc123"
const val DEFAULT_INVALID_URL = "https://invalid-url"
const val DEFAULT_BLANK_URL = ""


fun buildRequestDto(
    originalUrl: String = DEFAULT_VALID_URL
) = UrlRequestDto(originalUrl)

fun buildExpectedShortenResponse(
    originalUrl: String = DEFAULT_VALID_URL,
    shortUrl: String = DEFAULT_SHORT_URL
) = UrlResponseDto(
    originalUrl = originalUrl,
    shortUrl = shortUrl
)

fun buildExpectedError(
    status: Int = 400,
    message: String = "Something went wrong"
) = ErrorResponse(
    status = status,
    message = message
)

fun shortenUrl(
    restTemplate: TestRestTemplate,
    baseUrl: String,
    request: UrlRequestDto
): ResponseEntity<UrlResponseDto> =
    restTemplate.postForEntity(
        "$baseUrl/shorten",
        request,
        UrlResponseDto::class.java
    )

fun shortenUrlRaw(
    restTemplate: TestRestTemplate,
    baseUrl: String,
    request: UrlRequestDto
): ResponseEntity<String> =
    restTemplate.postForEntity(
        "$baseUrl/shorten",
        request,
        String::class.java
    )

fun redirect(
    restTemplate: TestRestTemplate,
    baseUrl: String,
    shortCode: String
): ResponseEntity<String> =
    restTemplate.getForEntity(
        "$baseUrl/$shortCode",
        String::class.java
    )

fun getOriginal(
    restTemplate: TestRestTemplate,
    baseUrl: String,
    shortCode: String
): ResponseEntity<String> =
    restTemplate.getForEntity(
        "$baseUrl/original/$shortCode",
        String::class.java
    )