package com.codefactory.shorty.domain.port

import com.codefactory.shorty.domain.model.UrlMapping

interface UrlRepositoryPort {

    fun save(urlMapping: UrlMapping): UrlMapping

    fun findByShortUrlCode(shortUrlCode: String): UrlMapping

    fun findByOriginalUrl(originalUrl: String): UrlMapping
}

sealed class UrlRepositoryPortError(message: String): RuntimeException(message){
    data class  UrlRepositoryPortNotFoundError(
        override val message: String
    ): UrlRepositoryPortError(message)

    data class UrlRepositoryPortUnexpectedError(
        override val message: String,
        override val cause: Throwable? = null,
    ): UrlRepositoryPortError(message)
}