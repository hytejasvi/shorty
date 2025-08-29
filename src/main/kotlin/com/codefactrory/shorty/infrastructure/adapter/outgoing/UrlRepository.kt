package com.codefactrory.shorty.infrastructure.adapter.outgoing

import com.codefactrory.shorty.domain.model.UrlMapping
import com.codefactrory.shorty.domain.port.UrlRepositoryPort

class UrlRepository(
    urlJpaRepository: UrlJpaRepository,
    ) : UrlRepositoryPort {

    override fun save(urlMapping: UrlMapping) {
        TODO("Not yet implemented")
    }

    override fun findByShortUrlCode(shortUrlCode: String) {
        TODO("Not yet implemented")
    }

    override fun findByOriginalUrl(originalUrl: String) {
        TODO("Not yet implemented")
    }
}