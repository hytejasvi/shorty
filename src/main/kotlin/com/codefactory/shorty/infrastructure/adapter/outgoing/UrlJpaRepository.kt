package com.codefactory.shorty.infrastructure.adapter.outgoing

import arrow.core.Either
import com.codefactory.shorty.domain.model.UrlMapping
import com.codefactory.shorty.domain.port.UrlRepositoryPortError
import org.springframework.data.jpa.repository.JpaRepository

interface UrlJpaRepository: JpaRepository<UrlMapping, Long> {
    fun findByShortUrlCode(shortUrlCode: String): UrlMapping?

    fun findByOriginalUrl(originalUrl: String): UrlMapping?
}