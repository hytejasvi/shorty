package com.codefactory.shorty.domain

import arrow.core.left
import arrow.core.right
import com.codefactory.shorty.application.service.UrlService
import com.codefactory.shorty.application.service.UrlServiceError
import com.codefactory.shorty.domain.model.UrlMapping
import com.codefactory.shorty.domain.port.UrlRepositoryPort
import com.codefactory.shorty.domain.port.UrlRepositoryPortError
import com.codefactory.shorty.domain.port.UrlRepositoryPortError.UrlRepositoryPortNotFoundError
import com.codefactory.shorty.infrastructure.adapter.incoming.UrlRequestDto
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any
import kotlin.test.assertEquals
import kotlin.test.assertIs

class UrlServiceTest {

    private lateinit var mockPort: UrlRepositoryPort
    private lateinit var service: UrlService

    companion object {
        private const val BASE_URL = "http://localhost:8080"
        private const val DEFAULT_ORIGINAL_URL = "https://www.dkbcodefactory.com/"
        private const val SHORT_CODE = "abc123"
    }

    @BeforeEach
    fun setup() {
        mockPort = mock(UrlRepositoryPort::class.java)
        service = UrlService(mockPort, BASE_URL, 6)
    }

    @Test
    fun `should return new short url when original not found`() {
        val request = UrlRequestDto(DEFAULT_ORIGINAL_URL)

        `when`(mockPort.findByOriginalUrl(DEFAULT_ORIGINAL_URL))
            .thenReturn(UrlRepositoryPortNotFoundError("Not found").left())

        `when`(mockPort.findByShortUrlCode(anyString()))
            .thenReturn(UrlRepositoryPortNotFoundError("Not found").left())

        mockSaveBehavior()

        val result = service.createShortUrl(request)

        assert(result.isRight())
        result.map { response ->
            assertEquals(DEFAULT_ORIGINAL_URL, response.originalUrl)
            assert(response.shortUrl.startsWith(BASE_URL))
        }

        verify(mockPort, times(1)).save(any())
    }

    @Test
    fun `should return existing short url when original already exists`() {
        val existingMapping = UrlMapping(
            id = 1L,
            originalUrl = DEFAULT_ORIGINAL_URL,
            shortUrlCode = SHORT_CODE
        )
        `when`(mockPort.findByOriginalUrl(DEFAULT_ORIGINAL_URL))
            .thenReturn(existingMapping.right())

        val result = service.createShortUrl(UrlRequestDto(DEFAULT_ORIGINAL_URL))

        assert(result.isRight())
        result.map { response ->
            assertEquals(DEFAULT_ORIGINAL_URL, response.originalUrl)
            assertEquals("$BASE_URL/$SHORT_CODE", response.shortUrl)
        }
        verify(mockPort, never()).save(any())
    }

    @Test
    fun `should propagate unexpected repository error when creating short url`() {
        val request = UrlRequestDto(DEFAULT_ORIGINAL_URL)
        `when`(mockPort.findByOriginalUrl(DEFAULT_ORIGINAL_URL))
            .thenReturn(UrlRepositoryPortError.UrlRepositoryPortUnexpectedError("DB failure").left())

        val result = service.createShortUrl(request)

        assert(result.isLeft())
        result.mapLeft { error ->
            assertIs<UrlServiceError.UrlServiceUnexpectedError>(error)
            assertEquals("DB failure", error.message)
        }
    }

    @Test
    fun `should return invalid input error for blank URL`() {
        val request = UrlRequestDto("")
        val result = service.createShortUrl(request)

        assert(result.isLeft())
        result.mapLeft { error ->
            assertIs<UrlServiceError.UrlServiceInvalidInputError>(error)
            assert(error.message.contains("Invalid"))
        }
    }

    @Test
    fun `should return invalid input error for malformed URL`() {
        val request = UrlRequestDto("not a url")
        val result = service.createShortUrl(request)

        assert(result.isLeft())
        result.mapLeft { error ->
            assertIs<UrlServiceError.UrlServiceInvalidInputError>(error)
            assert(error.message.contains("Invalid"))
        }
    }

    @Test
    fun `should return invalid input error for wrong scheme URL`() {
        val request = UrlRequestDto("abc://example.com")
        val result = service.createShortUrl(request)

        assert(result.isLeft())
        result.mapLeft { error ->
            assertIs<UrlServiceError.UrlServiceInvalidInputError>(error)
            assert(error.message.contains("Invalid"))
        }
    }

    @Test
    fun `should return original url when short code exists`() {
        val mapping = UrlMapping(
            id = 1L,
            originalUrl = DEFAULT_ORIGINAL_URL,
            shortUrlCode = SHORT_CODE
        )
        `when`(mockPort.findByShortUrlCode(SHORT_CODE))
            .thenReturn(mapping.right())

        val result = service.getOriginalUrl(SHORT_CODE)

        assert(result.isRight())
        result.map { response ->
            assertEquals(DEFAULT_ORIGINAL_URL, response.originalUrl)
        }
    }

    @Test
    fun `should return not found error when short code does not exist`() {
        `when`(mockPort.findByShortUrlCode(SHORT_CODE))
            .thenReturn(UrlRepositoryPortNotFoundError("Not found").left())

        val result = service.getOriginalUrl(SHORT_CODE)

        assert(result.isLeft())
        result.mapLeft { error ->
            assertIs<UrlServiceError.UrlServiceNotFoundError>(error)
            assertEquals("Not found", error.message)
        }
    }

    private fun mockSaveBehavior() {
        `when`(mockPort.save(any())).thenAnswer { invocation ->
            val mapping = invocation.getArgument<UrlMapping>(0)
            mapping.id = mapping.id ?: 1L
            mapping.right()
        }
    }
}
