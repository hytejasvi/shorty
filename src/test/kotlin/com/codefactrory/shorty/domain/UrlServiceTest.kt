package com.codefactrory.shorty.domain

import com.codefactrory.shorty.domain.model.UrlMapping
import com.codefactrory.shorty.domain.port.UrlRepositoryPort
import com.codefactrory.shorty.domain.port.UrlRepositoryPortError
import com.codefactrory.shorty.domain.port.UrlRepositoryPortError.UrlRepositoryPortNotFoundError
import com.codefactrory.shorty.domain.service.UrlService
import com.codefactrory.shorty.infrastructure.adapter.incoming.UrlRequestDto
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.mockito.Mockito.*
import org.mockito.kotlin.any

class UrlServiceTest {

    private lateinit var mockPort: UrlRepositoryPort
    private lateinit var service: UrlService

    @BeforeEach
    fun setup() {
        mockPort = mock(UrlRepositoryPort::class.java)
        service = UrlService(
            mockPort,
            BASE_URL,
            6
        )
    }

    @Test
    fun `should return new short url when original not found`() {
        val request = UrlRequestDto(DEFAULT_ORIGINAL_URL)

        `when`(mockPort.findByOriginalUrl(DEFAULT_ORIGINAL_URL))
            .thenThrow(UrlRepositoryPortNotFoundError("Not found by Original Url: $DEFAULT_ORIGINAL_URL"))

        mockSaveBehavior()

        val result = service.createShortUrl(request)

        assertEquals(DEFAULT_ORIGINAL_URL, result.originalUrl)
        assertTrue(result.shortUrl.startsWith(BASE_URL))
        verify(mockPort, times(2)).save(any())
    }

    @Test
    fun `should return existing short url when original already exists`() {
        val request = UrlRequestDto(DEFAULT_ORIGINAL_URL)
        val existingMapping = UrlMapping(
            id = 1L,
            originalUrl = DEFAULT_ORIGINAL_URL,
            shortUrlCode = SHORT_CODE
        )

        `when`(mockPort.findByOriginalUrl(DEFAULT_ORIGINAL_URL))
            .thenReturn(existingMapping)

        val result = service.createShortUrl(request)

        assertEquals(DEFAULT_ORIGINAL_URL, result.originalUrl)
        assertEquals("$BASE_URL/$SHORT_CODE", result.shortUrl)
        verify(mockPort, never()).save(any()) // no save because already exists
    }

    @Test
    fun `should propagate unexpected repository error`() {
        val request = UrlRequestDto(DEFAULT_ORIGINAL_URL)

        `when`(mockPort.findByOriginalUrl(DEFAULT_ORIGINAL_URL))
            .thenThrow(UrlRepositoryPortError.UrlRepositoryPortUnexpectedError("DB failure"))

        val ex = assertThrows(UrlRepositoryPortError.UrlRepositoryPortUnexpectedError::class.java) {
            service.createShortUrl(request)
        }

        assertEquals("DB failure", ex.message)
    }

    @Test
    fun `should return original url when short code exists`() {
        val mapping = UrlMapping(
            id = 1L,
            originalUrl = DEFAULT_ORIGINAL_URL,
            shortUrlCode = SHORT_CODE
        )

        `when`(mockPort.findByShortUrlCode(SHORT_CODE))
            .thenReturn(mapping)

        val result = service.getOriginalUrl(SHORT_CODE)

        assertEquals(DEFAULT_ORIGINAL_URL, result)
    }

    @Test
    fun `should throw not found error when short code does not exist`() {
        `when`(mockPort.findByShortUrlCode(SHORT_CODE))
            .thenThrow(UrlRepositoryPortNotFoundError("Not found by short code: $SHORT_CODE"))

        val ex = assertThrows(UrlRepositoryPortNotFoundError::class.java) {
            service.getOriginalUrl(SHORT_CODE)
        }

        assertEquals("Not found by short code: $SHORT_CODE", ex.message)
    }

    private fun mockSaveBehavior() {
        `when`(mockPort.save(any())).thenAnswer { invocation ->
            val mapping = invocation.getArgument<UrlMapping>(0)
            mapping.id = mapping.id ?: 1L
            mapping
        }
    }

    @Test
    fun `should throw IllegalArgumentException for invalid URL`() {
        val invalidUrls = listOf("", "not a url", "abc://example.com")
        for (url in invalidUrls) {
            val request = UrlRequestDto(url)
            val ex = assertThrows(IllegalArgumentException::class.java) {
                service.createShortUrl(request)
            }
            assertTrue(ex.message!!.contains("Invalid URL"))
        }
    }

    @Test
    fun `should normalize and accept valid URLs`() {
        val validUrls = mapOf(
            "example.com" to "https://example.com",
            "http://example.com" to "http://example.com",
            "https://example.com/path" to "https://example.com/path"
        )
        for ((input, expected) in validUrls) {
            `when`(mockPort.findByOriginalUrl(expected))
                .thenThrow(UrlRepositoryPortNotFoundError("Not found"))
            mockSaveBehavior()
            val result = service.createShortUrl(UrlRequestDto(input))
            assertEquals(expected, result.originalUrl)
        }
    }

    companion object {
        private const val BASE_URL = "http://localhost:8080"
        private const val DEFAULT_ORIGINAL_URL = "https://www.dkbcodefactory.com/"
        private const val SHORT_CODE = "abc123"
    }
}