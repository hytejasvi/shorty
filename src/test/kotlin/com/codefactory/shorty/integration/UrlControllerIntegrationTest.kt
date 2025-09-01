package com.codefactory.shorty.integration

import com.codefactory.shorty.domain.common.UrlNormalizer
import com.codefactory.shorty.fixtures.*
import com.codefactory.shorty.infrastructure.adapter.incoming.ErrorResponse
import com.fasterxml.jackson.databind.ObjectMapper
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

      @LocalServerPort
      private var port: Int = 0

      private lateinit var restTemplate: TestRestTemplate
      private lateinit var baseUrl: String

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
        val expected = buildExpectedShortenResponse()

        val response = shortenUrl(restTemplate, baseUrl, request)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertEquals(expected.originalUrl, response.body!!.originalUrl)
        assertTrue(response.body!!.shortUrl.startsWith("http://localhost"))
    }

    @Test
    fun `should return same short URL if original URL already exists`() {
        val request = buildRequestDto()
        val firstResponse = shortenUrl(restTemplate, baseUrl, request)
        val secondResponse = shortenUrl(restTemplate, baseUrl, request)

        assertEquals(firstResponse.body!!.shortUrl, secondResponse.body!!.shortUrl)
    }

    @Test
    fun `should redirect to original URL when short code exists`() {
        val request = buildRequestDto()
        val expectedResponse = shortenUrl(restTemplate, baseUrl, request)
        val shortCode = expectedResponse.body!!.shortUrl.substringAfterLast("/")

        val redirectResponse = redirect(restTemplate, baseUrl, shortCode)

        assertEquals(HttpStatus.FOUND, redirectResponse.statusCode)
        assertEquals(request.originalUrl, redirectResponse.headers.location.toString())
    }

    @Test
    fun `should return original URL when short code exists`() {
        val request = buildRequestDto()
        val expectedResponse = shortenUrl(restTemplate, baseUrl, request)
        val shortCode = expectedResponse.body!!.shortUrl.substringAfterLast("/")

        val response = getOriginal(restTemplate, baseUrl, shortCode)
        assertEquals(HttpStatus.OK, response.statusCode)
        assertEquals(request.originalUrl, response.body)
    }

    @Test
    fun `should return 404 when short code does not exist`() {
        val response = getOriginal(restTemplate, baseUrl, "test12")
        val expectedError = buildExpectedError(status = 404, message = "Actual url not found for short code: test12")

        assertEquals(HttpStatus.NOT_FOUND, response.statusCode)
        assertTrue(response.body!!.contains(expectedError.message!!))
    }

    @ParameterizedTest
    @ValueSource(strings = [DEFAULT_INVALID_URL, DEFAULT_BLANK_URL])
    fun `should return 400 for invalid or blank URLs`(url: String) {
        val request = buildRequestDto(originalUrl = url)
        val response = shortenUrlRaw(restTemplate, baseUrl, request)

        val expectedMessage = when (url) {
            DEFAULT_INVALID_URL -> "Invalid URL format: $DEFAULT_INVALID_URL"
            DEFAULT_BLANK_URL -> "Url should not be blank"
            else -> "Unexpected"
        }
        val expectedError = buildExpectedError(message = expectedMessage)

        val actualError = ObjectMapper().readValue(response.body, ErrorResponse::class.java)

        assertEquals(HttpStatus.BAD_REQUEST, response.statusCode)
        assertEquals(expectedError, actualError)
    }

    @ParameterizedTest
    @ValueSource(strings = [DEFAULT_VALID_URL, DEFAULT_VALID_URL_NO_PROTOCOL])
    fun `should shorten multiple valid URLs correctly`(url: String) {
        val request = buildRequestDto(originalUrl = url)
        val response = shortenUrl(restTemplate, baseUrl, request)

        assertEquals(HttpStatus.CREATED, response.statusCode)
        assertTrue(response.body!!.shortUrl.startsWith("http://localhost"))

        val expectedOriginalUrl = UrlNormalizer.normalizeAndValidate(url)
        assertEquals(expectedOriginalUrl, response.body!!.originalUrl)
    }
}