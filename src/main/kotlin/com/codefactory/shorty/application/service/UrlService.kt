package com.codefactory.shorty.application.service

import arrow.core.Either
import arrow.core.flatMap
import arrow.core.left
import arrow.core.right
import com.codefactory.shorty.domain.common.UrlNormalizer
import com.codefactory.shorty.domain.model.UrlMapping
import com.codefactory.shorty.application.service.UrlServiceError.UrlServiceNotFoundError
import com.codefactory.shorty.application.service.UrlServiceError.UrlServiceUnexpectedError
import com.codefactory.shorty.application.service.UrlServiceError.UrlServiceInvalidInputError
import com.codefactory.shorty.domain.BaseError
import com.codefactory.shorty.domain.port.UrlRepositoryPort
import com.codefactory.shorty.domain.port.UrlRepositoryPortError
import com.codefactory.shorty.domain.port.UrlRepositoryPortError.UrlRepositoryPortNotFoundError
import com.codefactory.shorty.domain.port.UrlRepositoryPortError.UrlRepositoryPortUnexpectedError
import com.codefactory.shorty.infrastructure.adapter.incoming.UrlRequestDto
import com.codefactory.shorty.infrastructure.adapter.incoming.UrlResponseDto
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import kotlin.random.Random

@Service
class UrlService(
    val urlRepositoryPort: UrlRepositoryPort,
    private val baseUrl: String = "http://localhost:8080",
    private val shortCodeLength: Int = 6
) {

    private val charset = ('0'..'9') + ('a'..'z') + ('A'..'Z')

    @Transactional
    fun createShortUrl(urlRequestDto: UrlRequestDto): Either<UrlServiceError, UrlResponseDto> =
        UrlNormalizer.normalizeAndValidate(urlRequestDto.originalUrl)
            .mapLeft { UrlServiceInvalidInputError("Invalid: ${urlRequestDto.originalUrl}") }
            .flatMap { normalizedUrl -> fetchOrCreateShortUrl(normalizedUrl) }

    private fun fetchOrCreateShortUrl(normalizedUrl: String): Either<UrlServiceError, UrlResponseDto> =
        urlRepositoryPort.findByOriginalUrl(normalizedUrl).fold(
            ifLeft = { error ->
                when (error) {
                    is UrlRepositoryPortNotFoundError -> {
                        val shortCode = generateUniqueShortCode()
                        urlRepositoryPort.save(
                            UrlMapping(originalUrl = normalizedUrl, shortUrlCode = shortCode)
                        ).getOrNull() ?: throw IllegalArgumentException("Failed to save URL mapping")
                        mapToResponse(normalizedUrl, shortCode).right()
                    }
                    else -> handleError(error).left()
                }
            },
            ifRight = { existing -> mapToResponse(existing.originalUrl, existing.shortUrlCode).right() }
        )

    fun getOriginalUrl(shortUrlCode: String): Either<UrlServiceError, OriginalUrlResponse> =
        urlRepositoryPort.findByShortUrlCode(shortUrlCode)
            .map { mapToOriginalUrl(it) }
            .mapLeft { error -> handleError(error)}

    private fun mapToResponse(originalUrl: String, shortUrlCode: String) =
        UrlResponseDto(
            originalUrl = originalUrl,
            shortUrl = "$baseUrl/${shortUrlCode}"
        )

    private fun mapToOriginalUrl(urlMapping: UrlMapping) =
        OriginalUrlResponse(urlMapping.originalUrl)

    private fun generateUniqueShortCode(): String {
        while (true) {
            val code = (1..shortCodeLength)
                .map { charset.random() }
                .joinToString("")

            val exists = urlRepositoryPort.findByShortUrlCode(code).isRight()
            if (!exists) return code
        }
    }

    private fun handleError(error: UrlRepositoryPortError): UrlServiceError =
        when (error) {
            is UrlRepositoryPortNotFoundError -> UrlServiceNotFoundError(error.message)
            is UrlRepositoryPortUnexpectedError -> UrlServiceUnexpectedError(error.message)
        }
}

data class OriginalUrlResponse(
    val originalUrl: String
)

sealed class UrlServiceError : BaseError() {
    data class UrlServiceNotFoundError(
        override val message: String
    ): UrlServiceError()

    data class UrlServiceInvalidInputError(
        override val message: String
    ): UrlServiceError()

    data class UrlServiceUnexpectedError(
        override val message: String,
    ): UrlServiceError()
}
