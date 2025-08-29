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
import org.mockito.kotlin.verify
import org.mockito.kotlin.times

class UrlServiceTest {

    private val DEFAULT_ORIGINAL_URL = "https://www.dkbcodefactory.com/"
    private lateinit var mockPort: UrlRepositoryPort
    private lateinit var service: UrlService

    @BeforeEach
    fun setup() {
        mockPort = mock(UrlRepositoryPort::class.java)
        service = UrlService(
            mockPort,
            "http://localhost:8080",
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
        assertTrue(result.shortUrl.startsWith("http://localhost:8080"))
        verify(mockPort, times(2)).save(any())
    }

    @Test
    fun `should return existing short url when original already exists`() {
        val request = UrlRequestDto(DEFAULT_ORIGINAL_URL)
        val existingMapping = UrlMapping(
            id = 1L,
            originalUrl = DEFAULT_ORIGINAL_URL,
            shortUrlCode = "abc123"
        )

        `when`(mockPort.findByOriginalUrl(DEFAULT_ORIGINAL_URL))
            .thenReturn(existingMapping)

        val result = service.createShortUrl(request)

        assertEquals(DEFAULT_ORIGINAL_URL, result.originalUrl)
        assertEquals("http://localhost:8080/abc123", result.shortUrl)
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

    private fun mockSaveBehavior() {
        `when`(mockPort.save(any())).thenAnswer { invocation ->
            val mapping = invocation.getArgument<UrlMapping>(0)
            mapping.id = mapping.id ?: 1L
            mapping
        }
    }
}