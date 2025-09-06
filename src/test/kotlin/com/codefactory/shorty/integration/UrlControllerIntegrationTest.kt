package com.codefactory.shorty.integration

import arrow.core.getOrElse
import com.codefactory.shorty.domain.common.UrlNormalizer
import com.codefactory.shorty.fixtures.*
import com.codefactory.shorty.infrastructure.adapter.incoming.ErrorResponse
import com.codefactory.shorty.infrastructure.adapter.incoming.UrlResponseDto
import com.codefactory.shorty.application.service.OriginalUrlResponse
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.boot.test.web.client.TestRestTemplate
import org.springframework.boot.test.web.server.LocalServerPort
import org.springframework.http.HttpStatus
import org.springframework.http.client.SimpleClientHttpRequestFactory
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class UrlControllerIntegrationTest {

    /*@LocalServerPort
    private var port: Int = 0

    private lateinit var restTemplate: TestRestTemplate
    private lateinit var baseUrl: String
    private val mapper = jacksonObjectMapper()

    @BeforeEach
    fun setup() {
        baseUrl = "http://localhost:$port"
        restTemplate = TestRestTemplate().apply {
            this.restTemplate.requestFactory = SimpleClientHttpRequestFactory().apply {
                java.net.HttpURLConnection.setFollowRedirects(false)
            }
        }
    }

    @Test
    fun `should create new short URL for valid original URL`() {
        val request = buildRequestDto()
        val response = shortenUrl(restTemplate, baseUrl, request)

        assertEquals(HttpStatus.CREATED, response.statusCode)

        val actual = mapper.convertValue(response.body, UrlResponseDto::class.java)
        assertEquals(request.originalUrl, actual.originalUrl)
        assertTrue(actual.shortUrl.startsWith("http://localhost"))
    }

    @Test
    fun `should return same short URL if original URL already exists`() {
        val request = buildRequestDto()
        val firstResponse = shortenUrl(restTemplate, baseUrl, request)
        val secondResponse = shortenUrl(restTemplate, baseUrl, request)

        val first = mapper.convertValue(firstResponse.body, UrlResponseDto::class.java)
        val second = mapper.convertValue(secondResponse.body, UrlResponseDto::class.java)

        assertEquals(first.shortUrl, second.shortUrl)
    }

    @Test
    fun `should redirect to original URL when short code exists`() {
        val request = buildRequestDto()
        val response = shortenUrl(restTemplate, baseUrl, request)
        val shortCode = mapper.convertValue(response.body, UrlResponseDto::class.java)
            .shortUrl.substringAfterLast("/")

        val redirectResponse = redirect(restTemplate, baseUrl, shortCode)
        assertEquals(HttpStatus.FOUND, redirectResponse.statusCode)
        assertEquals(request.originalUrl, redirectResponse.headers.location.toString())
    }

    @Test
    fun `should return original URL when short code exists`() {
        val request = buildRequestDto()
        val shortenResponse = shortenUrl(restTemplate, baseUrl, request)
        val shortCode = mapper.convertValue(shortenResponse.body, UrlResponseDto::class.java)
            .shortUrl.substringAfterLast("/")

        val originalResponse = getOriginal(restTemplate, baseUrl, shortCode)
        assertEquals(HttpStatus.OK, originalResponse.statusCode)

        val actual = mapper.readValue<OriginalUrlResponse>(originalResponse.body!!)
        val expectedOriginal = UrlNormalizer.normalizeAndValidate(request.originalUrl)
            .getOrElse { throw IllegalArgumentException("Invalid URL in test: ${request.originalUrl}") }

        assertEquals(expectedOriginal, actual.originalUrl)
    }

    @Test
    fun `should return 404 when short code does not exist`() {
        val response = getOriginal(restTemplate, baseUrl, "nonexistent")
        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)

        val actualError = mapper.readValue<ErrorResponse>(response.body!!)
        assertTrue(actualError.message!!.contains("not found", ignoreCase = true))
    }

    @ParameterizedTest
    @ValueSource(strings = [DEFAULT_INVALID_URL, DEFAULT_BLANK_URL])
    fun `should return 400 for invalid or blank URLs`(url: String) {
        val request = buildRequestDto(originalUrl = url)
        val response = shortenUrlRaw(restTemplate, baseUrl, request)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)

        val actualError = mapper.readValue<ErrorResponse>(response.body!!)
        val expectedMessage = when (url) {
            DEFAULT_INVALID_URL -> "Invalid URL format: $DEFAULT_INVALID_URL"
            DEFAULT_BLANK_URL -> "Url should not be blank"
            else -> "Unexpected"
        }
        assertEquals(expectedMessage, actualError.message)
    }

    @ParameterizedTest
    @ValueSource(strings = [DEFAULT_VALID_URL, DEFAULT_VALID_URL_NO_PROTOCOL])
    fun `should shorten multiple valid URLs correctly`(url: String) {
        val request = buildRequestDto(originalUrl = url)
        val response = shortenUrl(restTemplate, baseUrl, request)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        val actual = mapper.convertValue(response.body, UrlResponseDto::class.java)

        assertTrue(actual.shortUrl.startsWith("http://localhost"))

        val expectedOriginal = UrlNormalizer.normalizeAndValidate(url)
            .getOrElse { throw IllegalArgumentException("Invalid URL in test: $url") }

        assertEquals(expectedOriginal, actual.originalUrl)
    }*/
}
