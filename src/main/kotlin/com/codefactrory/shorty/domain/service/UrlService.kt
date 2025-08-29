package com.codefactrory.shorty.domain.service

import com.codefactrory.shorty.domain.common.Base62Encoder
import com.codefactrory.shorty.domain.common.UrlNormalizer
import com.codefactrory.shorty.domain.model.UrlMapping
import com.codefactrory.shorty.domain.port.UrlRepositoryPort
import com.codefactrory.shorty.domain.port.UrlRepositoryPortError
import com.codefactrory.shorty.infrastructure.adapter.incoming.UrlRequestDto
import com.codefactrory.shorty.infrastructure.adapter.incoming.UrlResponseDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class UrlService(
    val urlRepositoryPort: UrlRepositoryPort,
    @Value("\${shorty.base-url}") private val baseUrl: String,
    @Value("\${shorty.min-short-code-length}") private val minShortCodeLength: Int
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