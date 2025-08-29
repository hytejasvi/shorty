package com.codefactrory.shorty.domain.service

import com.codefactrory.shorty.domain.common.Base62Encoder
import com.codefactrory.shorty.domain.common.UrlNormalizer
import com.codefactrory.shorty.domain.model.UrlMapping
import com.codefactrory.shorty.domain.port.UrlRepositoryPort
import com.codefactrory.shorty.domain.port.UrlRepositoryPortError
import com.codefactrory.shorty.infrastructure.adapter.incoming.UrlRequestDto
import com.codefactrory.shorty.infrastructure.adapter.incoming.UrlResponseDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UrlService(
    val urlRepositoryPort: UrlRepositoryPort,
    private val baseUrl: String = "http://localhost:8080",
    private val minShortCodeLength: Int = 6
) {

    @Transactional
    fun createShortUrl(
        urlRequestDto: UrlRequestDto
    ): UrlResponseDto {
        val normalizedUrl = UrlNormalizer.normalizeAndValidate(urlRequestDto.originalUrl)
        val shortCode =
            try {
                val existingMapping = urlRepositoryPort.findByOriginalUrl(normalizedUrl)
                existingMapping.shortUrlCode
            } catch(e:UrlRepositoryPortError.UrlRepositoryPortNotFoundError) {
                generateNewShortUrlCode(normalizedUrl)
            }
        val urlResponse = UrlResponseDto(
            originalUrl = normalizedUrl,
            shortUrl = "$baseUrl/$shortCode"
        )
        return urlResponse
    }

    fun getOriginalUrl(
        shortUrlCode: String
    ): String {
        val urlMapping = urlRepositoryPort.findByShortUrlCode(shortUrlCode)
        return urlMapping.originalUrl
    }

    private fun generateNewShortUrlCode(
        originalUrl: String
    ): String {
        val savedMapping = urlRepositoryPort.save(
            UrlMapping(
                originalUrl = originalUrl,
                shortUrlCode = "",
                )
        )
        val obfuscatedNumber = obfuscatedId(savedMapping.id!!)
        val shortUrlCode = Base62Encoder.encode(obfuscatedNumber, minShortCodeLength)

        savedMapping.shortUrlCode = shortUrlCode

        urlRepositoryPort.save(savedMapping)

        return shortUrlCode
    }

    private fun obfuscatedId(id: Long): Long = (id * 12345) + 98765
}