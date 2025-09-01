package com.codefactory.shorty.infrastructure.adapter.outgoing

import com.codefactory.shorty.domain.model.UrlMapping
import org.springframework.data.jpa.repository.JpaRepository

interface UrlJpaRepository: JpaRepository<UrlMapping, Long> {

    fun findByShortUrlCode(shortUrlCode: String): UrlMapping?

    fun findByOriginalUrl(originalUrl: String): UrlMapping?
}