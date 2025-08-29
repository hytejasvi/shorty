package com.codefactrory.shorty.infrastructure.adapter.outgoing

import com.codefactrory.shorty.domain.model.UrlMapping
import org.springframework.data.jpa.repository.JpaRepository

interface UrlJpaRepository: JpaRepository<UrlMapping, Long> {

    fun findByShortUrlCode(shortUrlCode: String)

    fun findByOriginalUrl(originalUrl: String)
}