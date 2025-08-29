package com.codefactrory.shorty.domain.port

import com.codefactrory.shorty.domain.model.UrlMapping

interface UrlRepositoryPort {

    fun save(urlMapping: UrlMapping)

    fun findByShortUrlCode(shortUrlCode: String)

    fun findByOriginalUrl(originalUrl: String)
}