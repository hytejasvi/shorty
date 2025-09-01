package com.codefactory.shorty.infrastructure.adapter.outgoing

import com.codefactory.shorty.domain.model.UrlMapping
import com.codefactory.shorty.domain.port.UrlRepositoryPort
import com.codefactory.shorty.domain.port.UrlRepositoryPortError.UrlRepositoryPortNotFoundError
import com.codefactory.shorty.domain.port.UrlRepositoryPortError.UrlRepositoryPortUnexpectedError
import org.springframework.stereotype.Repository

@Repository
class UrlRepository(
    val urlJpaRepository: UrlJpaRepository,
    ) : UrlRepositoryPort {

    override fun save(urlMapping: UrlMapping): UrlMapping {
        return try {
            urlJpaRepository.save(urlMapping)
        } catch (e:java.lang.Exception) {
            throw UrlRepositoryPortUnexpectedError(
                "Unexpected Error while saving url Mapping: $urlMapping",
                e
            )
        }
    }

    override fun findByShortUrlCode(shortUrlCode: String): UrlMapping {
        return try {
            urlJpaRepository.findByShortUrlCode(shortUrlCode)
                ?: throw UrlRepositoryPortNotFoundError("Actual url not found for short code: $shortUrlCode")
        } catch (e: Exception) {
            if (e is UrlRepositoryPortNotFoundError) throw e
            throw UrlRepositoryPortUnexpectedError(
                "Unexpected error while fetching URL mapping for short code: $shortUrlCode",
                e
            )
        }
    }

    override fun findByOriginalUrl(originalUrl: String): UrlMapping {
        return try {
            urlJpaRepository.findByOriginalUrl(originalUrl)
                ?: throw UrlRepositoryPortNotFoundError("Short code not found for Url: $originalUrl")
        } catch (e: Exception) {
            if (e is UrlRepositoryPortNotFoundError) throw e
            throw UrlRepositoryPortUnexpectedError(
                "Unexpected error while fetching URL mapping from original url: $originalUrl",
                e
            )
        }
    }
}