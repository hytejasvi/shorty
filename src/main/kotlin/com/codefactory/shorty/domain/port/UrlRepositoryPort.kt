package com.codefactory.shorty.domain.port

import arrow.core.Either
import com.codefactory.shorty.domain.BaseError
import com.codefactory.shorty.domain.model.UrlMapping

interface UrlRepositoryPort {

    fun save(urlMapping: UrlMapping): Either<UrlRepositoryPortError, UrlMapping>

    fun findByShortUrlCode(shortUrlCode: String): Either<UrlRepositoryPortError, UrlMapping>

    fun findByOriginalUrl(originalUrl: String): Either<UrlRepositoryPortError, UrlMapping>
}

sealed class UrlRepositoryPortError: BaseError(){
    data class  UrlRepositoryPortNotFoundError(
        override val message: String
    ): UrlRepositoryPortError()

    data class UrlRepositoryPortUnexpectedError(
        override val message: String,
    ): UrlRepositoryPortError()
}