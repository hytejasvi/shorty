package com.codefactory.shorty.infrastructure.adapter.outgoing

import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.codefactory.shorty.domain.BaseError
import com.codefactory.shorty.domain.model.UrlMapping
import com.codefactory.shorty.domain.port.UrlRepositoryPort
import com.codefactory.shorty.domain.port.UrlRepositoryPortError
import com.codefactory.shorty.infrastructure.adapter.outgoing.UrlRepositoryError.UrlRepositoryShortCodeNotFoundError
import com.codefactory.shorty.infrastructure.adapter.outgoing.UrlRepositoryError.UrlRepositoryOriginalUrlNotFoundError
import com.codefactory.shorty.domain.port.UrlRepositoryPortError.UrlRepositoryPortNotFoundError
import com.codefactory.shorty.domain.port.UrlRepositoryPortError.UrlRepositoryPortUnexpectedError
import org.springframework.stereotype.Repository

@Repository
class UrlRepository(
    val urlJpaRepository: UrlJpaRepository,
    ) : UrlRepositoryPort {

    override fun save(urlMapping: UrlMapping): Either<UrlRepositoryPortError, UrlMapping> =
        try {
            urlJpaRepository.save(urlMapping).right()
        } catch (ex: Exception) {
            UrlRepositoryPortUnexpectedError(
                ex.message ?: "Unexpected error while saving",
            ).left()
        }

    override fun findByShortUrlCode(shortUrlCode: String): Either<UrlRepositoryPortError, UrlMapping> =
        try {
            val entity = urlJpaRepository.findByShortUrlCode(shortUrlCode)
            entity?.right() ?: UrlRepositoryPortNotFoundError("Short code $shortUrlCode not found").left()
        } catch (ex: Exception) {
            UrlRepositoryPortUnexpectedError(ex.message ?: "Unexpected error").left()
        }

    override fun findByOriginalUrl(originalUrl: String): Either<UrlRepositoryPortError, UrlMapping> =
        try {
            val entity = urlJpaRepository.findByOriginalUrl(originalUrl)
            entity?.right() ?: UrlRepositoryPortNotFoundError("Original URL $originalUrl not found").left()
        } catch (ex: Exception) {
            UrlRepositoryPortUnexpectedError(ex.message ?: "Unexpected error").left()
        }
}
sealed class UrlRepositoryError: BaseError() {
    data class  UrlRepositoryOriginalUrlNotFoundError(
        override val message: String
    ): UrlRepositoryError()

    data class  UrlRepositoryShortCodeNotFoundError(
        override val message: String
    ): UrlRepositoryError()
}