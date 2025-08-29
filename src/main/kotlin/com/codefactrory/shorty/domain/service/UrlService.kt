package com.codefactrory.shorty.domain.service

import com.codefactrory.shorty.domain.model.UrlMapping
import com.codefactrory.shorty.domain.port.UrlRepositoryPort
import com.codefactrory.shorty.domain.port.UrlRepositoryPortError
import com.codefactrory.shorty.infrastructure.adapter.incoming.UrlRequestDto
import com.codefactrory.shorty.infrastructure.adapter.incoming.UrlResponseDto
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service

@Service
class UrlService(
    val urlRepositoryPort: UrlRepositoryPort
) {

    private val BASE62 =  "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"

    @Value("\${shorty.base-url}")
    lateinit var baseUrl: String

    @Value("\${shorty.min-short-code-length}")
    private var minShortCodeLength: Int = 6


    fun createShortUrl(urlRequestDto: UrlRequestDto): UrlResponseDto {
        val shortCode =
            try {
                val existingMapping = urlRepositoryPort.findByOriginalUrl(urlRequestDto.originalUrl)
                existingMapping.shortUrlCode
            } catch(e:UrlRepositoryPortError.UrlRepositoryPortNotFoundError) {
                generateNewShortUrlCode(urlRequestDto.originalUrl)
            }
        val urlResponse = UrlResponseDto(
            originalUrl = urlRequestDto.originalUrl,
            shortUrl = shortCode,
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
        val shortUrlCode = base62EncodeWithMinLength(obfuscatedNumber, minShortCodeLength)
        savedMapping.shortUrlCode = shortUrlCode
        urlRepositoryPort.save(savedMapping)
        return shortUrlCode
    }

    fun base62EncodeWithMinLength(number: Long, minLength: Int): String
    { var n = number
        val sb = StringBuilder()
        do {
            val rem = (n % 62).toInt()
            sb.append(BASE62[rem])
            n /= 62
        } while (n > 0)
     while (sb.length < minLength) {
         sb.append('0')
     }
        return sb.reverse().toString()
    }

    private fun obfuscatedId(id: Long): Long = (id * 12345) + 98765
}